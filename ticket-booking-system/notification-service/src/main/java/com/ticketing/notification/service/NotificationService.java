package com.ticketing.notification.service;

import com.ticketing.common.dto.PaymentResultEvent;
import com.ticketing.notification.model.NotificationLog;
import com.ticketing.notification.model.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailNotifier            emailNotifier;
    private final NotificationLogRepository logRepository;

    // ─────────────────────────────────────────────────────────────
    // HANDLE PAYMENT RESULT
    // Called by NotificationConsumer when payment.result is received.
    // Routes to correct email template based on payment status.
    // ─────────────────────────────────────────────────────────────
    public void handlePaymentResult(PaymentResultEvent event) {

        String notifEvent = "BOOKING_" + event.getStatus(); // BOOKING_SUCCESS etc.

        // Idempotency — don't send same notification twice
        if (logRepository.existsByBookingIdAndEvent(event.getBookingId(), notifEvent)) {
            log.warn("Notification already sent for bookingId={}, event={}",
                    event.getBookingId(), notifEvent);
            return;
        }

        // Route based on payment status
        switch (event.getStatus()) {
            case "SUCCESS"   -> sendBookingConfirmed(event);
            case "FAILED"    -> sendBookingFailed(event);
            case "CANCELLED" -> sendBookingCancelled(event);
            default          -> log.warn("Unknown payment status: {}", event.getStatus());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // BOOKING CONFIRMED
    // ─────────────────────────────────────────────────────────────
    private void sendBookingConfirmed(PaymentResultEvent event) {
        // In production fetch user email from user-service or DB
        // For now we simulate with userId as recipient placeholder
        String recipient = "user-" + event.getUserId() + "@ticketing.com";

        String subject = "Booking Confirmed! ";
        String body = String.format("""
                Hi there!
                
                Your booking has been confirmed!
                
                Booking ID : %s
                Amount Paid: ₹%.2f
                
                Enjoy the event!
                Team Ticketing
                """,
                event.getBookingId(),
                event.getAmount());

        sendAndLog(recipient, subject, body, event, "BOOKING_SUCCESS");
    }

    // ─────────────────────────────────────────────────────────────
    // BOOKING FAILED
    // ─────────────────────────────────────────────────────────────
    private void sendBookingFailed(PaymentResultEvent event) {
        String recipient = "user-" + event.getUserId() + "@ticketing.com";

        String subject = "Booking Failed ";
        String body = String.format("""
                Hi there!
                
                Unfortunately your booking could not be completed.
                
                Booking ID : %s
                Reason     : %s
                
                Your seat has been released. Please try again.
                Team Ticketing
                """,
                event.getBookingId(),
                event.getFailureReason() != null ? event.getFailureReason() : "Payment failed");

        sendAndLog(recipient, subject, body, event, "BOOKING_FAILED");
    }

    // ─────────────────────────────────────────────────────────────
    // BOOKING CANCELLED
    // ─────────────────────────────────────────────────────────────
    private void sendBookingCancelled(PaymentResultEvent event) {
        String recipient = "user-" + event.getUserId() + "@ticketing.com";

        String subject = "Booking Cancelled";
        String body = String.format("""
                Hi there!
                
                Your booking has been cancelled.
                
                Booking ID : %s
                
                If you paid, a refund will be processed within 5-7 business days.
                Team Ticketing
                """,
                event.getBookingId());

        sendAndLog(recipient, subject, body, event, "BOOKING_CANCELLED");
    }

    // ─────────────────────────────────────────────────────────────
    // SEND + LOG
    // Sends email and logs the result in DB for deduplication/audit
    // ─────────────────────────────────────────────────────────────
    private void sendAndLog(String recipient, String subject, String body,
                            PaymentResultEvent event, String notifEvent) {
        NotificationLog.NotificationLogBuilder logBuilder = NotificationLog.builder()
                .bookingId(event.getBookingId())
                .userId(event.getUserId())
                .type("EMAIL")
                .event(notifEvent)
                .recipient(recipient);

        try {
            emailNotifier.send(recipient, subject, body);
            logBuilder.status("SENT");
            log.info("Notification sent: bookingId={}, event={}", event.getBookingId(), notifEvent);
        } catch (Exception e) {
            logBuilder.status("FAILED").failureReason(e.getMessage());
            log.error("Notification failed: bookingId={}, reason={}", event.getBookingId(), e.getMessage());
        } finally {
            logRepository.save(logBuilder.build());
        }
    }
}