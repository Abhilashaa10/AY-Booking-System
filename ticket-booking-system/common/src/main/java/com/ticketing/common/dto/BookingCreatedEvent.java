package com.ticketing.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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