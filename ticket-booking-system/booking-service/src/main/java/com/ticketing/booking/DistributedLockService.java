package com.ticketing.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {

    private final RedissonClient redissonClient;

    private static final String SEAT_LOCK_PREFIX = "lock:seat:";
    private static final long WAIT_TIME_SECONDS  = 3;   // max time to wait for lock
    private static final long LEASE_TIME_SECONDS = 10;  // auto-release after 10s if not released

    /**
     * Acquires a per-seat distributed lock, runs the supplier, then releases.
     * If lock cannot be acquired within WAIT_TIME_SECONDS → throws SeatAlreadyBookedException.
     */
    public <T> T executeWithLock(String seatId, Supplier<T> task) {
        String lockKey = SEAT_LOCK_PREFIX + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            acquired = lock.tryLock(WAIT_TIME_SECONDS, LEASE_TIME_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("Could not acquire lock for seat {}", seatId);
                throw new com.ticketing.booking.exception.SeatAlreadyBookedException(seatId);
            }
            log.debug("Lock acquired for seat {}", seatId);
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock acquisition interrupted for seat: " + seatId, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Lock released for seat {}", seatId);
            }
        }
    }
}