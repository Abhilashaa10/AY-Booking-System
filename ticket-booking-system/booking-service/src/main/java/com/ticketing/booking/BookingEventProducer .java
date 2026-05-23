package com.ticketing.booking.kafka;

import com.ticketing.booking.model.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.booking-created}")
    private String bookingCreatedTopic;

    /**
     * Publishes a BookingCreatedEvent to Kafka.
     * Key = bookingId — ensures all events for same booking go to same partition (ordering).
     */
    public void publishBookingCreated(BookingCreatedEvent event) {
        String key = event.getBookingId().toString();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(bookingCreatedTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish BookingCreatedEvent for bookingId={}: {}",
                        event.getBookingId(), ex.getMessage());
            } else {
                log.info("BookingCreatedEvent published: bookingId={}, partition={}, offset={}",
                        event.getBookingId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}