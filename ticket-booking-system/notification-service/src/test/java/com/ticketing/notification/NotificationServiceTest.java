package com.ticketing.notification;

import com.ticketing.common.dto.PaymentResultEvent;
import com.ticketing.notification.model.NotificationLog;
import com.ticketing.notification.model.NotificationLogRepository;
import com.ticketing.notification.service.EmailNotifier;
import com.ticketing.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock EmailNotifier             emailNotifier;
    @Mock NotificationLogRepository logRepository;

    @InjectMocks NotificationService notificationService;

    PaymentResultEvent successEvent;
    PaymentResultEvent failedEvent;

    @BeforeEach
    void setUp() {
        successEvent = PaymentResultEvent.builder()
                .bookingId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .seatId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(500))
                .status("SUCCESS")
                .processedAt(LocalDateTime.now())
                .build();

        failedEvent = PaymentResultEvent.builder()
                .bookingId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .seatId(UUID.randomUUID())
                .amount(BigDecimal.valueOf(500))
                .status("FAILED")
                .failureReason("Insufficient funds")
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void handlePaymentResult_success_sendsConfirmationEmail() {
        when(logRepository.existsByBookingIdAndEvent(any(), anyString())).thenReturn(false);
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.handlePaymentResult(successEvent);

        verify(emailNotifier).send(anyString(), anyString(), anyString());
        verify(logRepository).save(any(NotificationLog.class));
    }

    @Test
    void handlePaymentResult_failed_sendsFailureEmail() {
        when(logRepository.existsByBookingIdAndEvent(any(), anyString())).thenReturn(false);
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.handlePaymentResult(failedEvent);

        verify(emailNotifier).send(anyString(), anyString(), anyString());
    }

    @Test
    void handlePaymentResult_idempotency_skipsIfAlreadySent() {
        when(logRepository.existsByBookingIdAndEvent(any(), anyString())).thenReturn(true);

        notificationService.handlePaymentResult(successEvent);

        verify(emailNotifier, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void handlePaymentResult_emailFails_logsFailure() {
        when(logRepository.existsByBookingIdAndEvent(any(), anyString())).thenReturn(false);
        doThrow(new RuntimeException("SMTP error"))
                .when(emailNotifier).send(anyString(), anyString(), anyString());
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.handlePaymentResult(successEvent);

        verify(logRepository).save(any(NotificationLog.class));
    }
}