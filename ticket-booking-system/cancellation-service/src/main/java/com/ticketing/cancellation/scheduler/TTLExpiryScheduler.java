package com.ticketing.cancellation.scheduler;

import com.ticketing.cancellation.service.CancellationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TTLExpiryScheduler {

    private final CancellationService cancellationService;
    private final JdbcTemplate        jdbcTemplate;

    /**
     * Runs every 60 seconds.
     * Scans bookings table for PENDING bookings past their expires_at.
     * Cancels each one and releases the seat.
     *
     * Why here and not in booking-service?
     * Separation of concerns — cancellation-service owns all cancellation logic.
     * booking-service only creates bookings.
     */
    @Scheduled(fixedDelay = 60000) // every 60 seconds
    public void expireStaleBookings() {
        log.debug("TTL expiry scheduler running...");

        // Query booking-service DB for expired PENDING bookings
        List<Map<String, Object>> expiredBookings = jdbcTemplate.queryForList("""
                SELECT id, user_id, seat_id
                FROM bookings
                WHERE status = 'PENDING'
                AND expires_at < NOW()
                """);

        if (expiredBookings.isEmpty()) {
            log.debug("No expired bookings found");
            return;
        }

        log.info("Found {} expired bookings — processing cancellations", expiredBookings.size());

        for (Map<String, Object> row : expiredBookings) {
            UUID bookingId = UUID.fromString(row.get("id").toString());
            UUID userId    = row.get("user_id") != null
                    ? UUID.fromString(row.get("user_id").toString()) : null;
            UUID seatId    = row.get("seat_id") != null
                    ? UUID.fromString(row.get("seat_id").toString()) : null;

            try {
                cancellationService.cancelDueToTTLExpiry(bookingId, seatId, userId);
                log.info("TTL expired booking cancelled: bookingId={}", bookingId);
            } catch (Exception e) {
                log.error("Failed to cancel expired booking {}: {}", bookingId, e.getMessage());
            }
        }
    }
}