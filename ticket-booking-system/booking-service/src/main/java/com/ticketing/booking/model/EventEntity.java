package com.ticketing.booking.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEntity {
    
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "venue", nullable = false, length = 300)
    private String venue;
    
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    
    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}