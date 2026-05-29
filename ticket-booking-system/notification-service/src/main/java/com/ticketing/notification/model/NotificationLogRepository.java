package com.ticketing.notification.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByBookingId(UUID bookingId);

    // Deduplication check — don't send same notification twice
    boolean existsByBookingIdAndEvent(UUID bookingId, String event);
}