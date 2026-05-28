package com.ticketing.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    private static final String IDEM_PREFIX      = "idem:booking:";
    private static final Duration IDEM_TTL       = Duration.ofHours(24);

    /**
     * Returns the cached response if this key was already processed.
     * Returns empty if this is a new request.
     */
    public Optional<String> getCachedResponse(String idempotencyKey) {
        String value = redisTemplate.opsForValue().get(IDEM_PREFIX + idempotencyKey);
        if (value != null) {
            log.info("Idempotency hit for key: {}", idempotencyKey);
        }
        return Optional.ofNullable(value);
    }

    /**
     * Stores the response JSON so duplicate requests return the same result.
     */
    public void storeResponse(String idempotencyKey, String responseJson) {
        redisTemplate.opsForValue().set(IDEM_PREFIX + idempotencyKey, responseJson, IDEM_TTL);
        log.debug("Stored idempotency response for key: {}", idempotencyKey);
    }
}