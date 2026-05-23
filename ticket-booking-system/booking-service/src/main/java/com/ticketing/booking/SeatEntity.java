package com.ticketing.booking.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
    name = "seats",
    indexes = {
        @Index(name = "idx_seat_event_id", columnList = "event_id"),
        @Index(name = "idx_seat_status",   columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Column(name = "row_label", length = 5)
    private String rowLabel;

    @Column(name = "section", length = 20)
    private String section;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status;

    // Optimistic lock — prevents concurrent DB writes to same row
    @Version
    private Long version;

    public enum SeatStatus {
        AVAILABLE,
        HELD,       // TTL hold active (Redis key exists)
        BOOKED      // payment confirmed
    }
}