package com.ticketing.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.booking.exception.SeatAlreadyBookedException;
import com.ticketing.booking.kafka.BookingEventProducer;
import com.ticketing.booking.model.BookingEntity;
import com.ticketing.booking.model.SeatEntity;
import com.ticketing.booking.repository.BookingRepository;
import com.ticketing.booking.repository.SeatRepository;
import com.ticketing.booking.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository      bookingRepository;
    @Mock SeatRepository         seatRepository;
    @Mock SeatService            seatService;
    @Mock DistributedLockService lockService;
    @Mock IdempotencyService     idempotencyService;
    @Mock BookingEventProducer   eventProducer;
    @Mock ObjectMapper           objectMapper;

    @InjectMocks BookingService bookingService;

    UUID userId    = UUID.randomUUID();
    UUID seatId    = UUID.randomUUID();
    UUID eventId   = UUID.randomUUID();
    String idemKey = "test-idem-key-001";

    SeatEntity availableSeat;

    @BeforeEach
    void setUp() {
        availableSeat = SeatEntity.builder()
                .id(seatId)
                .eventId(eventId)
                .seatNumber("A1")
                .price(BigDecimal.valueOf(500))
                .status(SeatEntity.SeatStatus.AVAILABLE)
                .build();
    }

    @Test
    void createBooking_success() {
        // Arrange
        when(idempotencyService.getCachedResponse(idemKey)).thenReturn(Optional.empty());
        when(lockService.executeWithLock(eq(seatId.toString()), any())).thenAnswer(inv ->
                inv.getArgument(1, java.util.function.Supplier.class).get());
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(availableSeat));
        when(seatService.holdSeat(seatId, userId)).thenReturn(true);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        BookingEntity result = bookingService.createBooking(userId, seatId, eventId, idemKey);

        // Assert
        assertThat(result.getStatus()).isEqualTo(BookingEntity.BookingStatus.PENDING);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getSeatId()).isEqualTo(seatId);
        verify(eventProducer).publishBookingCreated(any());
        verify(seatRepository).save(argThat(s -> s.getStatus() == SeatEntity.SeatStatus.HELD));
    }

    @Test
    void createBooking_seatAlreadyHeld_throwsException() {
        when(idempotencyService.getCachedResponse(idemKey)).thenReturn(Optional.empty());
        when(lockService.executeWithLock(eq(seatId.toString()), any())).thenAnswer(inv ->
                inv.getArgument(1, java.util.function.Supplier.class).get());
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(availableSeat));
        when(seatService.holdSeat(seatId, userId)).thenReturn(false); // already held

        assertThatThrownBy(() -> bookingService.createBooking(userId, seatId, eventId, idemKey))
                .isInstanceOf(SeatAlreadyBookedException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_seatStatusNotAvailable_throwsException() {
        availableSeat.setStatus(SeatEntity.SeatStatus.BOOKED);
        when(idempotencyService.getCachedResponse(idemKey)).thenReturn(Optional.empty());
        when(lockService.executeWithLock(eq(seatId.toString()), any())).thenAnswer(inv ->
                inv.getArgument(1, java.util.function.Supplier.class).get());
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(availableSeat));

        assertThatThrownBy(() -> bookingService.createBooking(userId, seatId, eventId, idemKey))
                .isInstanceOf(SeatAlreadyBookedException.class);
    }

    @Test
    void cancelBooking_wrongUser_throwsSecurityException() {
        BookingEntity booking = BookingEntity.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID()) // different user
                .seatId(seatId)
                .status(BookingEntity.BookingStatus.PENDING)
                .build();

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(booking.getId(), userId))
                .isInstanceOf(SecurityException.class);
    }
}