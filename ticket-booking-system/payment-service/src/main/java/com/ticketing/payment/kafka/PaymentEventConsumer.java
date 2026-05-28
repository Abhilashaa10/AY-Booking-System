package com.ticketing.payment.kafka;

import com.ticketing.common.dto.BookingCreatedEvent;
import com.ticketing.payment.service.PaymentService;
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
public class PaymentEventConsumer {

    private final PaymentService paymentService;

    /**
     * Listens on booking.created topic.
     * groupId = payment-service-group ensures each message is processed
     * by only ONE instance of payment-service (even if scaled horizontally).
     *
     * Manual ack — we only commit the offset AFTER successfully processing,
     * so if the service crashes mid-processing the message is re-delivered.
     */
    @KafkaListener(
        topics       = "${kafka.topics.booking-created}",
        groupId      = "payment-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onBookingCreated(
            @Payload BookingCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {
        log.info("Received BookingCreatedEvent: bookingId={}, partition={}, offset={}",
                event.getBookingId(), partition, offset);

        try {
            paymentService.processPayment(event);
            // Commit offset only after successful processing
            ack.acknowledge();
            log.info("Offset committed for bookingId={}", event.getBookingId());
        } catch (Exception ex) {
            log.error("Failed to process payment for bookingId={}: {}",
                    event.getBookingId(), ex.getMessage(), ex);
            // Do NOT ack — message will be redelivered after retry backoff
            // In production add a Dead Letter Topic (DLT) for poison pills
        }
    }
}