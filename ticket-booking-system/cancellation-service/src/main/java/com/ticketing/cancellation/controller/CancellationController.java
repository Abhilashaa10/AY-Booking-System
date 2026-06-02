package com.ticketing.cancellation.controller;

import com.ticketing.cancellation.model.CancellationEntity;
import com.ticketing.cancellation.service.CancellationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cancellations")
@RequiredArgsConstructor
public class CancellationController {

    private final CancellationService cancellationService;

    /**
     * POST /api/v1/cancellations/{bookingId}
     * User explicitly cancels a booking.
     * Headers: X-User-Id, X-Seat-Id
     */
    @PostMapping("/{bookingId}")
    public ResponseEntity<CancellationEntity> cancelBooking(
            @PathVariable                      UUID bookingId,
            @RequestHeader("X-User-Id")        @NotNull UUID userId,
            @RequestHeader("X-Seat-Id")        @NotNull UUID seatId
    ) {
        CancellationEntity cancellation =
                cancellationService.cancelByUser(bookingId, userId, seatId);
        return ResponseEntity.ok(cancellation);
    }
}