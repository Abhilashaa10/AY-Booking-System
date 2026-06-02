package com.ticketing.cancellation.kafka;

import com.ticketing.common.dto.PaymentResultEvent;
import com.ticketing.cancellation.service.CancellationService;
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
public class PaymentResultConsumer {

    private final CancellationService cancellationService;

    /**
     * Listens on payment.result topic.
     * If payment FAILED or CANCELLED — auto cancel the booking
     * and release the seat back to available pool.
     */
    @KafkaListener(
        topics           = "${kafka.topics.payment-result}",
        groupId          = "cancellation-service-group",
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
            // Only act on failed/cancelled payments
            if ("FAILED".equals(event.getStatus()) || "CANCELLED".equals(event.getStatus())) {
                cancellationService.cancelDueToPaymentFailure(
                        event.getBookingId(),
                        event.getSeatId(),
                        event.getUserId(),
                        event.getStatus()
                );
            }
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process cancellation for bookingId={}: {}",
                    event.getBookingId(), ex.getMessage(), ex);
        }
    }
}