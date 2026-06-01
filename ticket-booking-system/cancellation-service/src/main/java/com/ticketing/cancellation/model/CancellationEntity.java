package com.ticketing.cancellation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "cancellations",
    indexes = {
        @Index(name = "idx_cancellation_booking_id", columnList = "booking_id", unique = true),
        @Index(name = "idx_cancellation_user_id",    columnList = "user_id"),
        @Index(name = "idx_cancellation_reason",     columnList = "reason")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private UUID bookingId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "seat_id")
    private UUID seatId;

    // USER_INITIATED, TTL_EXPIRED, PAYMENT_FAILED
    @Column(nullable = false, length = 30)
    private String reason;

    // PENDING, COMPLETED, REFUNDED
    @Column(nullable = false, length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}