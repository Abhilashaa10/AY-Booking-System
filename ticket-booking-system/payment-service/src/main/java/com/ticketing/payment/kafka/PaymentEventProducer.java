package com.ticketing.payment.kafka;

import com.ticketing.common.dto.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.payment-result}")
    private String paymentResultTopic;

    /**
     * Publishes PaymentResultEvent to payment.result topic.
     * Key = bookingId — booking-service and notification-service both consume this.
     */
    public void publishPaymentResult(PaymentResultEvent event) {
        String key = event.getBookingId().toString();

        kafkaTemplate.send(paymentResultTopic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish PaymentResultEvent for bookingId={}: {}",
                            event.getBookingId(), ex.getMessage());
                } else {
                    log.info("PaymentResultEvent published: bookingId={}, status={}, partition={}, offset={}",
                            event.getBookingId(),
                            event.getStatus(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
    }
}