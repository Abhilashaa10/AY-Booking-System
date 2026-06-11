package com.ticketing.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {

    /**
     * Logs every incoming request and outgoing response.
     * Useful for debugging and monitoring traffic through the gateway.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();

        String method = exchange.getRequest().getMethod().name();
        String path   = exchange.getRequest().getURI().getPath();
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        log.info("→ {} {} [userId={}]", method, path, userId);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration   = System.currentTimeMillis() - startTime;
            int  statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;

            log.info("← {} {} [status={}, {}ms]", method, path, statusCode, duration);
        }));
    }

    @Override
    public int getOrder() {
        return -2; // runs before AuthFilter
    }
}