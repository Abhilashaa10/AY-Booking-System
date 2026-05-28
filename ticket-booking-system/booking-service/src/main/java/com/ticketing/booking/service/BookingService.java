package com.ticketing.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.booking.exception.BookingNotFoundException;
import com.ticketing.booking.exception.SeatAlreadyBookedException;
import com.ticketing.booking.kafka.BookingEventProducer;
import com.ticketing.booking.model.BookingEntity;
import com.ticketing.booking.model.BookingEntity.BookingStatus;
import com.ticketing.booking.model.SeatEntity;
import com.ticketing.common.dto.BookingCreatedEvent;
import com.ticketing.booking.repository.BookingRepository;
import com.ticketing.booking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository      bookingRepository;
    private final SeatRepository         seatRepository;
    private final SeatService            seatService;
    private final DistributedLockService lockService;
    private final IdempotencyService     idempotencyService;
    private final BookingEventProducer   eventProducer;
    private final ObjectMapper           objectMapper;

    private static final int HOLD_MINUTES = 10;

    // ─────────────────────────────────────────────────────────────
    // CREATE BOOKING
    // Flow:
    //  1. Idempotency check (Redis) — return cached response if duplicate
    //  2. Acquire distributed lock on seatId (Redisson)
    //  3. Check seat is AVAILABLE in DB
    //  4. Place TTL hold in Redis (SETNX)
    //  5. Save PENDING booking in PostgreSQL
    //  6. Publish booking.created to Kafka
    //  7. Cache response for idempotency
    //  8. Release lock
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public BookingEntity createBooking(UUID userId, UUID seatId,
                                       UUID eventId, String idempotencyKey) {

        // Step 1 — Idempotency check
        var cached = idempotencyService.getCachedResponse(idempotencyKey);
        if (cached.isPresent()) {
            try {
                log.info("Returning cached response for idempotency key: {}", idempotencyKey);
                return objectMapper.readValue(cached.get(), BookingEntity.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize cached booking, re-processing", e);
            }
        }

        // Steps 2–7 run inside the distributed lock
        return lockService.executeWithLock(seatId.toString(), () -> {

            // Step 2 — Verify seat exists and is AVAILABLE
            SeatEntity seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatId));

            if (seat.getStatus() != SeatEntity.SeatStatus.AVAILABLE) {
                throw new SeatAlreadyBookedException(seatId.toString());
            }

            // Step 3 — Place TTL hold in Redis
            boolean held = seatService.holdSeat(seatId, userId);
            if (!held) {
                throw new SeatAlreadyBookedException(seatId.toString());
            }

            // Step 4 — Mark seat as HELD in DB
            seat.setStatus(SeatEntity.SeatStatus.HELD);
            seatRepository.save(seat);

            // Step 5 — Persist PENDING booking
            BookingEntity booking = BookingEntity.builder()
                    .userId(userId)
                    .seatId(seatId)
                    .eventId(eventId)
                    .status(BookingStatus.PENDING)
                    .idempotencyKey(idempotencyKey)
                    .amount(seat.getPrice())
                    .expiresAt(LocalDateTime.now().plusMinutes(HOLD_MINUTES))
                    .build();

            booking = bookingRepository.save(booking);
            log.info("Booking created: id={}, seat={}, user={}", booking.getId(), seatId, userId);

            // Step 6 — Publish to Kafka (payment-service will consume this)
            BookingCreatedEvent event = BookingCreatedEvent.builder()
                    .bookingId(booking.getId())
                    .userId(userId)
                    .seatId(seatId)
                    .eventId(eventId)
                    .amount(seat.getPrice())
                    .idempotencyKey(idempotencyKey)
                    .createdAt(booking.getCreatedAt())
                    .build();

            eventProducer.publishBookingCreated(event);

            // Step 7 — Cache for idempotency
            try {
                idempotencyService.storeResponse(idempotencyKey, objectMapper.writeValueAsString(booking));
            } catch (JsonProcessingException e) {
                log.warn("Failed to cache idempotency response for key: {}", idempotencyKey);
            }

            return booking;
        });
    }

    // ─────────────────────────────────────────────────────────────
    // GET BOOKING
    // ─────────────────────────────────────────────────────────────
    public BookingEntity getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    // ─────────────────────────────────────────────────────────────
    // CONFIRM BOOKING (called by payment-service result consumer)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void confirmBooking(UUID bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Cannot confirm booking {} — current status: {}", bookingId, booking.getStatus());
            return;
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        seatService.confirmSeatBooked(booking.getSeatId());
        log.info("Booking {} CONFIRMED", bookingId);
    }

    // ─────────────────────────────────────────────────────────────
    // FAIL BOOKING (called on payment failure)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void failBooking(UUID bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        booking.setStatus(BookingStatus.FAILED);
        bookingRepository.save(booking);

        // Release seat hold so other users can book
        seatService.releaseSeatHold(booking.getSeatId());

        // Mark seat AVAILABLE again in DB
        seatRepository.findById(booking.getSeatId()).ifPresent(seat -> {
            seat.setStatus(SeatEntity.SeatStatus.AVAILABLE);
            seatRepository.save(seat);
        });

        log.info("Booking {} FAILED — seat released", bookingId);
    }

    // ─────────────────────────────────────────────────────────────
    // CANCEL BOOKING (user-initiated)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void cancelBooking(UUID bookingId, UUID requestingUserId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!booking.getUserId().equals(requestingUserId)) {
            throw new SecurityException("User " + requestingUserId + " cannot cancel booking " + bookingId);
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            seatService.releaseSeatHold(booking.getSeatId());

            seatRepository.findById(booking.getSeatId()).ifPresent(seat -> {
                seat.setStatus(SeatEntity.SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            });

            log.info("Booking {} CANCELLED by user {}", bookingId, requestingUserId);
        } else {
            throw new IllegalStateException("Cannot cancel booking in status: " + booking.getStatus());
        }
    }
}