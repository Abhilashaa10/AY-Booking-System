package com.ticketing.booking.model.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreatedEvent {

    private UUID bookingId;
    private UUID userId;
    private UUID seatId;
    private UUID eventId;
    private BigDecimal amount;
    private String idempotencyKey;
    private LocalDateTime createdAt;
}