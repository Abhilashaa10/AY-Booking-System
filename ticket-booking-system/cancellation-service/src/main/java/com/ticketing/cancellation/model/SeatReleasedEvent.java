package com.ticketing.cancellation.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatReleasedEvent {

    private UUID bookingId;
    private UUID seatId;
    private UUID userId;
    private String reason;         // USER_INITIATED, TTL_EXPIRED, PAYMENT_FAILED
    private LocalDateTime releasedAt;
}