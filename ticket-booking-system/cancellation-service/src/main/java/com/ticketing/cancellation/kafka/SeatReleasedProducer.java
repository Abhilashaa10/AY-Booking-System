package com.ticketing.cancellation.kafka;

import com.ticketing.cancellation.model.SeatReleasedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeatReleasedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.seat-released}")
    private String seatReleasedTopic;

    /**
     * Publishes SeatReleasedEvent to seat.released topic.
     * booking-service consumes this to mark seat AVAILABLE again in DB.
     */
    public void publishSeatReleased(SeatReleasedEvent event) {
        String key = event.getBookingId().toString();

        kafkaTemplate.send(seatReleasedTopic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish SeatReleasedEvent for bookingId={}: {}",
                            event.getBookingId(), ex.getMessage());
                } else {
                    log.info("SeatReleasedEvent published: bookingId={}, reason={}, partition={}, offset={}",
                            event.getBookingId(),
                            event.getReason(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
    }
}