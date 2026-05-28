package com.ticketing.booking.repository;

import com.ticketing.booking.model.BookingEntity;
import com.ticketing.booking.model.BookingEntity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

    Optional<BookingEntity> findByIdempotencyKey(String idempotencyKey);

    List<BookingEntity> findByUserIdAndStatus(UUID userId, BookingStatus status);

    // Fetch all PENDING bookings past their expiry — used by TTL scheduler
    @Query("SELECT b FROM BookingEntity b WHERE b.status = 'PENDING' AND b.expiresAt < :now")
    List<BookingEntity> findExpiredPendingBookings(@Param("now") LocalDateTime now);

    // Bulk expire — more efficient than saving one by one
    @Modifying
    @Query("UPDATE BookingEntity b SET b.status = 'EXPIRED' WHERE b.status = 'PENDING' AND b.expiresAt < :now")
    int bulkExpirePendingBookings(@Param("now") LocalDateTime now);
}