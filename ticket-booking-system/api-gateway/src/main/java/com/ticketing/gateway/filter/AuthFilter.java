package com.ticketing.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    /**
     * Runs on EVERY request before routing.
     *
     * For now validates that X-User-Id header is present.
     * In production replace with JWT token validation:
     *   1. Extract Bearer token from Authorization header
     *   2. Validate signature using public key
     *   3. Extract userId from claims
     *   4. Forward userId as X-User-Id header to downstream service
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Skip auth for health check endpoints
        if (path.contains("/actuator") || path.contains("/health")) {
            return chain.filter(exchange);
        }

        // Check X-User-Id header is present
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId == null || userId.isBlank()) {
            log.warn("Request rejected — missing X-User-Id header: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        log.debug("Request authorized for userId={}, path={}", userId, path);
        return chain.filter(exchange);
    }

    // Run before other filters
    @Override
    public int getOrder() {
        return -1;
    }
}