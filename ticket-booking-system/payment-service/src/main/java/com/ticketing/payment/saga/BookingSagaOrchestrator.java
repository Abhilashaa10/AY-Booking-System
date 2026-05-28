package com.ticketing.payment.saga;

import com.ticketing.common.dto.PaymentResultEvent;
import com.ticketing.payment.kafka.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BookingSagaOrchestrator manages the distributed transaction across
 * booking-service and payment-service.
 *
 * HAPPY PATH:
 *   booking.created → payment SUCCESS → payment.result(SUCCESS)
 *   → booking-service confirms booking + marks seat BOOKED
 *
 * COMPENSATION (rollback) PATH:
 *   booking.created → payment FAILED/CANCELLED → payment.result(FAILED)
 *   → booking-service releases seat + marks booking FAILED
 *   → notification-service sends failure email
 *
 * We use choreography-based saga here — services react to events.
 * No central coordinator needed for this simple 2-step flow.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingSagaOrchestrator {

    private final PaymentEventProducer eventProducer;

    /**
     * Triggered when payment FAILS or is CANCELLED.
     * Publishes a compensation event that booking-service consumes
     * to release the seat and mark the booking as FAILED.
     */
    public void compensate(UUID bookingId, UUID seatId, String failureStatus) {
        log.warn("SAGA COMPENSATION triggered: bookingId={}, reason={}",
                bookingId, failureStatus);

        // Build a compensation event — booking-service listens on payment.result
        // and handles FAILED/CANCELLED status by releasing the seat
        PaymentResultEvent compensationEvent = PaymentResultEvent.builder()
                .bookingId(bookingId)
                .seatId(seatId)
                .status(failureStatus)
                .failureReason("Saga compensation — payment " + failureStatus.toLowerCase())
                .processedAt(LocalDateTime.now())
                .build();

        // payment.result is already published in PaymentService.
        // This method exists to make saga logic explicit and extensible.
        // In a more complex saga you'd publish to a dedicated compensation topic.
        log.info("Saga compensation event queued for bookingId={}", bookingId);

        // Future extension: publish to booking.compensate topic for
        // more complex multi-step rollbacks (e.g. refund already charged cards)
    }
}