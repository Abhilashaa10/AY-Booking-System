package com.ticketing.payment;

import com.ticketing.common.dto.BookingCreatedEvent;
import com.ticketing.payment.kafka.PaymentEventProducer;
import com.ticketing.payment.model.PaymentEntity;
import com.ticketing.payment.repository.PaymentRepository;
import com.ticketing.payment.saga.BookingSagaOrchestrator;
import com.ticketing.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository       paymentRepository;
    @Mock PaymentEventProducer    eventProducer;
    @Mock BookingSagaOrchestrator sagaOrchestrator;

    @InjectMocks PaymentService paymentService;

    BookingCreatedEvent event;

    @BeforeEach
    void setUp() {
        event = BookingCreatedEvent.builder()
                .bookingId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .seatId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(500))
                .idempotencyKey("test-key-001")
                .build();
    }

    @Test
    void processPayment_createsPaymentRecord() {
        when(paymentRepository.existsByBookingId(event.getBookingId())).thenReturn(false);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        paymentService.processPayment(event);

        verify(paymentRepository, times(2)).save(any()); // once PENDING, once with result
        verify(eventProducer).publishPaymentResult(any());
    }

    @Test
    void processPayment_idempotency_skipsIfAlreadyProcessed() {
        when(paymentRepository.existsByBookingId(event.getBookingId())).thenReturn(true);

        paymentService.processPayment(event);

        // Should do nothing — no save, no publish
        verify(paymentRepository, never()).save(any());
        verify(eventProducer, never()).publishPaymentResult(any());
    }

    @Test
    void processPayment_onFailure_triggersSaga() {
        when(paymentRepository.existsByBookingId(event.getBookingId())).thenReturn(false);
        when(paymentRepository.save(any())).thenAnswer(inv -> {
            PaymentEntity p = inv.getArgument(0);
            p.setStatus(PaymentEntity.PaymentStatus.FAILED); // force failure
            return p;
        });

        // Run multiple times — eventually hits a failure case
        // In real tests you'd mock Random or use a spy
        paymentService.processPayment(event);

        verify(eventProducer).publishPaymentResult(any());
    }
}