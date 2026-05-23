package com.ticketing.booking.controller;

import com.ticketing.booking.model.BookingEntity;
import com.ticketing.booking.service.BookingService;
import com.ticketing.booking.service.SeatService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SeatService    seatService;

    /**
     * POST /api/v1/bookings
     * Headers: X-User-Id, X-Idempotency-Key
     */
    @PostMapping("/bookings")
    public ResponseEntity<BookingEntity> createBooking(
            @RequestHeader("X-User-Id")           @NotNull UUID userId,
            @RequestHeader("X-Idempotency-Key")   @NotNull String idempotencyKey,
            @RequestParam                          @NotNull UUID seatId,
            @RequestParam                          @NotNull UUID eventId
    ) {
        BookingEntity booking = bookingService.createBooking(userId, seatId, eventId, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    /**
     * GET /api/v1/bookings/{bookingId}
     */
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingEntity> getBooking(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getBooking(bookingId));
    }

    /**
     * DELETE /api/v1/bookings/{bookingId}
     * Headers: X-User-Id
     */
    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") @NotNull UUID userId
    ) {
        bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/events/{eventId}/seats
     * Returns all available seats for an event
     */
    @GetMapping("/events/{eventId}/seats")
    public ResponseEntity<List<?>> getAvailableSeats(@PathVariable UUID eventId) {
        return ResponseEntity.ok(seatService.getAvailableSeats(eventId));
    }
}