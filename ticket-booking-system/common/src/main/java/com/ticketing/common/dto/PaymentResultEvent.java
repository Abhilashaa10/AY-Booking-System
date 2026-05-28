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
public class PaymentResultEvent {

    private UUID bookingId;
    private UUID userId;
    private UUID seatId;
    private BigDecimal amount;
    private String status;
    private String failureReason;
    private LocalDateTime processedAt;
}