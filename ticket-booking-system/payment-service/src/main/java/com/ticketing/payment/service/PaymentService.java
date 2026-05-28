package com.ticketing.payment.service;

import com.ticketing.common.dto.BookingCreatedEvent;
import com.ticketing.common.dto.PaymentResultEvent;
import com.ticketing.payment.kafka.PaymentEventProducer;
import com.ticketing.payment.model.PaymentEntity;
import com.ticketing.payment.model.PaymentEntity.PaymentStatus;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.payment.saga.BookingSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository      paymentRepository;
    private final PaymentEventProducer   eventProducer;
    private final BookingSagaOrchestrator sagaOrchestrator;

    private static final Random RANDOM = new Random();

    // ─────────────────────────────────────────────────────────────
    // PROCESS PAYMENT
    // Called by PaymentEventConsumer when booking.created is received.
    //
    // Flow:
    //  1. Idempotency check — don't process same booking twice
    //  2. Create PENDING payment record in DB
    //  3. Simulate payment result (SUCCESS / FAILED / CANCELLED)
    //  4. Update payment status in DB
    //  5. Publish payment.result to Kafka
    //  6. If FAILED/CANCELLED → saga compensates (releases seat)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void processPayment(BookingCreatedEvent event) {

        // Step 1 — Idempotency: skip if already processed
        if (paymentRepository.existsByBookingId(event.getBookingId())) {
            log.warn("Payment already processed for bookingId={} — skipping",
                    event.getBookingId());
            return;
        }

        // Step 2 — Create PENDING payment record
        PaymentEntity payment = PaymentEntity.builder()
                .bookingId(event.getBookingId())
                .userId(event.getUserId())
                .amount(event.getAmount())
                .status(PaymentStatus.PENDING)
                .gateway("SIMULATED")
                .idempotencyKey(event.getIdempotencyKey())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment record created: id={}, bookingId={}",
                payment.getId(), event.getBookingId());

        // Step 3 — Simulate payment result
        // Weights: 80% SUCCESS, 15% FAILED, 5% CANCELLED
        PaymentStatus result = simulatePayment();
        log.info("Simulated payment result: {} for bookingId={}",
                result, event.getBookingId());

        // Step 4 — Update payment status in DB
        payment.setStatus(result);
        payment.setProcessedAt(LocalDateTime.now());

        if (result == PaymentStatus.FAILED) {
            payment.setFailureReason("Simulated payment failure — insufficient funds");
        } else if (result == PaymentStatus.CANCELLED) {
            payment.setFailureReason("Simulated payment cancelled by user");
        }

        paymentRepository.save(payment);

        // Step 5 — Publish result to Kafka
        // booking-service and notification-service both consume payment.result
        PaymentResultEvent resultEvent = PaymentResultEvent.builder()
                .bookingId(event.getBookingId())
                .userId(event.getUserId())
                .seatId(event.getSeatId())
                .amount(event.getAmount())
                .status(result.name())
                .failureReason(payment.getFailureReason())
                .processedAt(payment.getProcessedAt())
                .build();

        eventProducer.publishPaymentResult(resultEvent);

        // Step 6 — Saga compensation on failure
        if (result == PaymentStatus.FAILED || result == PaymentStatus.CANCELLED) {
            log.warn("Payment {} for bookingId={} — triggering saga compensation",
                    result, event.getBookingId());
            sagaOrchestrator.compensate(event.getBookingId(), event.getSeatId(), result.name());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GET PAYMENT STATUS (REST endpoint)
    // ─────────────────────────────────────────────────────────────
    public PaymentEntity getPaymentByBookingId(UUID bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found for bookingId: " + bookingId));
    }

    // ─────────────────────────────────────────────────────────────
    // SIMULATE PAYMENT
    // 80% SUCCESS, 15% FAILED, 5% CANCELLED
    // Replace this with Razorpay/Stripe SDK call in production.
    // ─────────────────────────────────────────────────────────────
    private PaymentStatus simulatePayment() {
        int roll = RANDOM.nextInt(100); // 0-99
        if (roll < 80) return PaymentStatus.SUCCESS;
        if (roll < 95) return PaymentStatus.FAILED;
        return PaymentStatus.CANCELLED;
    }
}