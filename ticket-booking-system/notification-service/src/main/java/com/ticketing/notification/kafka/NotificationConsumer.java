package com.ticketing.notification.kafka;

import com.ticketing.common.dto.PaymentResultEvent;
import com.ticketing.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    /**
     * Listens on payment.result topic.
     * Completely independent of booking-service and payment-service —
     * it only reacts to events, never calls them directly.
     *
     * groupId = notification-service-group ensures this consumer group
     * gets its own copy of every message (separate from booking-service
     * which also consumes payment.result).
     */
    @KafkaListener(
        topics           = "${kafka.topics.payment-result}",
        groupId          = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentResult(
            @Payload PaymentResultEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {
        log.info("Received PaymentResultEvent: bookingId={}, status={}, partition={}, offset={}",
                event.getBookingId(), event.getStatus(), partition, offset);

        try {
            notificationService.handlePaymentResult(event);
            ack.acknowledge(); // commit offset only after success
        } catch (Exception ex) {
            log.error("Failed to process notification for bookingId={}: {}",
                    event.getBookingId(), ex.getMessage(), ex);
            // Do NOT ack — message will be redelivered
        }
    }
}