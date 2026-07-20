package com.ticketing.booking.repository;

import com.ticketing.booking.model.SeatEntity;
import com.ticketing.booking.model.SeatEntity.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {

    List<SeatEntity> findByEventIdAndStatus(UUID eventId, SeatStatus status);

    // Pessimistic write lock — DB-level fallback if Redis lock is unavailable
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatEntity s WHERE s.id = :id")
    Optional<SeatEntity> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT COUNT(s) FROM SeatEntity s WHERE s.eventId = :eventId AND s.status = 'AVAILABLE'")
    long countAvailableByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT COUNT(s) FROM SeatEntity s WHERE s.eventId = :eventId AND s.status = :status")
Long countByEventIdAndStatus(UUID eventId, String status);
}