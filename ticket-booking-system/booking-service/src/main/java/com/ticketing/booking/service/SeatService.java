package com.ticketing.booking.service;

import com.ticketing.booking.model.SeatEntity;
import com.ticketing.booking.model.SeatEntity.SeatStatus;
import com.ticketing.booking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final SeatRepository seatRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String SEAT_STATUS_PREFIX = "seat:status:";
    private static final String SEAT_HOLD_PREFIX   = "seat:hold:";
    private static final Duration CACHE_TTL        = Duration.ofMinutes(5);
    private static final Duration HOLD_TTL         = Duration.ofMinutes(10);

    /**
     * Returns all AVAILABLE seats for an event.
     * Tries Redis cache first; falls back to PostgreSQL and refreshes cache.
     */
    public List<SeatEntity> getAvailableSeats(UUID eventId) {
        // For simplicity we query DB directly and let @Cacheable handle it.
        // In production you'd store a Set of available seatIds in Redis.
        return seatRepository.findByEventIdAndStatus(eventId, SeatStatus.AVAILABLE);
    }

    /**
     * Places a TTL hold on a seat in Redis.
     * Key: seat:hold:{seatId}  Value: userId  TTL: 10 min
     * Returns true if hold was placed, false if seat was already held.
     */
    public boolean holdSeat(UUID seatId, UUID userId) {
        String key = SEAT_HOLD_PREFIX + seatId;
        // SETNX — only sets if key doesn't exist
        Boolean set = redisTemplate.opsForValue()
                .setIfAbsent(key, userId.toString(), HOLD_TTL);
        boolean held = Boolean.TRUE.equals(set);
        if (held) {
            log.info("Seat {} held by user {} for {} min", seatId, userId, HOLD_TTL.toMinutes());
        } else {
            log.warn("Seat {} is already held", seatId);
        }
        return held;
    }

    /**
     * Releases the TTL hold from Redis (called on cancellation or payment failure).
     */
    public void releaseSeatHold(UUID seatId) {
        redisTemplate.delete(SEAT_HOLD_PREFIX + seatId);
        log.info("Seat hold released for seat {}", seatId);
    }

    /**
     * Checks whether a seat currently has an active hold in Redis.
     */
    public boolean isSeatHeld(UUID seatId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(SEAT_HOLD_PREFIX + seatId));
    }

    /**
     * Marks seat as BOOKED in PostgreSQL (called after payment confirmation).
     */
    public void confirmSeatBooked(UUID seatId) {
        seatRepository.findById(seatId).ifPresent(seat -> {
            seat.setStatus(SeatStatus.BOOKED);
            seatRepository.save(seat);
            redisTemplate.delete(SEAT_HOLD_PREFIX + seatId);
            log.info("Seat {} marked as BOOKED", seatId);
        });
    }
}