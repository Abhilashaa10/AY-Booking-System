package com.ticketing.cancellation.service;

import com.ticketing.cancellation.kafka.SeatReleasedProducer;
import com.ticketing.cancellation.model.CancellationEntity;
import com.ticketing.cancellation.model.CancellationRepository;
import com.ticketing.cancellation.model.SeatReleasedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationService {

    private final CancellationRepository cancellationRepository;
    private final SeatReleasedProducer   seatReleasedProducer;
    private final StringRedisTemplate    redisTemplate;

    private static final String SEAT_HOLD_PREFIX = "seat:hold:";

    // ─────────────────────────────────────────────────────────────
    // USER INITIATED CANCELLATION
    // Called from REST endpoint when user explicitly cancels
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public CancellationEntity cancelByUser(UUID bookingId, UUID userId, UUID seatId) {

        // Idempotency — don't cancel twice
        if (cancellationRepository.existsByBookingId(bookingId)) {
            log.warn("Cancellation already exists for bookingId={}", bookingId);
            return cancellationRepository.findByBookingId(bookingId).orElseThrow();
        }

        log.info("User-initiated cancellation: bookingId={}, userId={}", bookingId, userId);
        return processCancellation(bookingId, userId, seatId, "USER_INITIATED");
    }

    // ─────────────────────────────────────────────────────────────
    // PAYMENT FAILURE CANCELLATION
    // Called by PaymentResultConsumer when payment FAILED/CANCELLED
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void cancelDueToPaymentFailure(UUID bookingId, UUID seatId,
                                          UUID userId, String paymentStatus) {

        if (cancellationRepository.existsByBookingId(bookingId)) {
            log.warn("Cancellation already exists for bookingId={}", bookingId);
            return;
        }

        String reason = "PAYMENT_" + paymentStatus; // PAYMENT_FAILED or PAYMENT_CANCELLED
        log.info("Payment failure cancellation: bookingId={}, reason={}", bookingId, reason);
        processCancellation(bookingId, userId, seatId, reason);
    }

    // ─────────────────────────────────────────────────────────────
    // TTL EXPIRY CANCELLATION
    // Called by TTLExpiryScheduler when booking hold expires
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void cancelDueToTTLExpiry(UUID bookingId, UUID seatId, UUID userId) {

        if (cancellationRepository.existsByBookingId(bookingId)) {
            return; // already cancelled
        }

        log.info("TTL expiry cancellation: bookingId={}", bookingId);
        processCancellation(bookingId, userId, seatId, "TTL_EXPIRED");
    }

    // ─────────────────────────────────────────────────────────────
    // CORE CANCELLATION LOGIC
    // 1. Release Redis seat hold
    // 2. Save cancellation record
    // 3. Publish seat.released to Kafka
    // ─────────────────────────────────────────────────────────────
    private CancellationEntity processCancellation(UUID bookingId, UUID userId,
                                                   UUID seatId, String reason) {
        // Step 1 — Release Redis hold immediately
        if (seatId != null) {
            redisTemplate.delete(SEAT_HOLD_PREFIX + seatId);
            log.info("Redis seat hold released for seatId={}", seatId);
        }

        // Step 2 — Save cancellation record
        CancellationEntity cancellation = CancellationEntity.builder()
                .bookingId(bookingId)
                .userId(userId)
                .seatId(seatId)
                .reason(reason)
                .status("COMPLETED")
                .completedAt(LocalDateTime.now())
                .build();

        cancellation = cancellationRepository.save(cancellation);
        log.info("Cancellation saved: id={}, bookingId={}, reason={}",
                cancellation.getId(), bookingId, reason);

        // Step 3 — Publish seat.released to Kafka
        // booking-service consumes this and marks seat AVAILABLE in DB
        SeatReleasedEvent event = SeatReleasedEvent.builder()
                .bookingId(bookingId)
                .seatId(seatId)
                .userId(userId)
                .reason(reason)
                .releasedAt(LocalDateTime.now())
                .build();

        seatReleasedProducer.publishSeatReleased(event);

        return cancellation;
    }
}