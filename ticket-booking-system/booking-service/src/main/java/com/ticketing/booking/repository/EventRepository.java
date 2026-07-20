package com.ticketing.booking.repository;

import com.ticketing.booking.model.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    
    @Query("SELECT COUNT(s) FROM SeatEntity s WHERE s.eventId = :eventId AND s.status = 'AVAILABLE'")
    Long countAvailableSeatsByEventId(UUID eventId);
}