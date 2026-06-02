package com.ticketing.cancellation;

import com.ticketing.cancellation.kafka.SeatReleasedProducer;
import com.ticketing.cancellation.model.CancellationEntity;
import com.ticketing.cancellation.model.CancellationRepository;
import com.ticketing.cancellation.service.CancellationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancellationServiceTest {

    @Mock CancellationRepository cancellationRepository;
    @Mock SeatReleasedProducer   seatReleasedProducer;
    @Mock StringRedisTemplate    redisTemplate;

    @InjectMocks CancellationService cancellationService;

    UUID bookingId = UUID.randomUUID();
    UUID userId    = UUID.randomUUID();
    UUID seatId    = UUID.randomUUID();

    @Test
    void cancelByUser_success() {
        when(cancellationRepository.existsByBookingId(bookingId)).thenReturn(false);
        when(cancellationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CancellationEntity result =
                cancellationService.cancelByUser(bookingId, userId, seatId);

        assertThat(result.getReason()).isEqualTo("USER_INITIATED");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        verify(seatReleasedProducer).publishSeatReleased(any());
        verify(redisTemplate).delete("seat:hold:" + seatId);
    }

    @Test
    void cancelByUser_idempotency_skipsIfAlreadyCancelled() {
        when(cancellationRepository.existsByBookingId(bookingId)).thenReturn(true);
        when(cancellationRepository.findByBookingId(bookingId))
                .thenReturn(java.util.Optional.of(new CancellationEntity()));

        cancellationService.cancelByUser(bookingId, userId, seatId);

        verify(cancellationRepository, never()).save(any());
        verify(seatReleasedProducer, never()).publishSeatReleased(any());
    }

    @Test
    void cancelDueToPaymentFailure_success() {
        when(cancellationRepository.existsByBookingId(bookingId)).thenReturn(false);
        when(cancellationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cancellationService.cancelDueToPaymentFailure(bookingId, seatId, userId, "FAILED");

        verify(cancellationRepository).save(argThat(c ->
                "PAYMENT_FAILED".equals(c.getReason())));
        verify(seatReleasedProducer).publishSeatReleased(any());
    }

    @Test
    void cancelDueToTTLExpiry_success() {
        when(cancellationRepository.existsByBookingId(bookingId)).thenReturn(false);
        when(cancellationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        cancellationService.cancelDueToTTLExpiry(bookingId, seatId, userId);

        verify(cancellationRepository).save(argThat(c ->
                "TTL_EXPIRED".equals(c.getReason())));
        verify(seatReleasedProducer).publishSeatReleased(any());
    }
}