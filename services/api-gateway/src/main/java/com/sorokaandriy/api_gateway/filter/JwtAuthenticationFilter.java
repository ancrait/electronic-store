package com.sorokaandriy.api_gateway.filter;

import com.sorokaandriy.api_gateway.configuration.JwtService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Rejects requests to protected routes without a valid JWT and enriches
 * forwarded requests with the resolved user id and role.
 * Each downstream service validates the token again on its own.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh"
    );

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isPublic(request)) {
            return chain.filter(exchange);
        }

        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "Authorization header is missing");
        }

        String token = header.substring(BEARER_PREFIX.length());

        if (!jwtService.isTokenValid(token)) {
            return unauthorized(exchange, "Token is invalid or expired");
        }

        Claims claims = jwtService.parseToken(token);

        ServerWebExchange mutated = exchange.mutate()
                .request(builder -> builder
                        .header("X-User-Id", claims.getSubject())
                        .header("X-User-Role", String.valueOf(claims.get("role", String.class))))
                .build();

        return chain.filter(mutated);
    }

    private boolean isPublic(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if (HttpMethod.OPTIONS.equals(method)) {
            return true;
        }

        if (PUBLIC_ENDPOINTS.contains(path)) {
            return true;
        }

        return HttpMethod.GET.equals(method)
                && (path.startsWith("/api/products") || path.startsWith("/api/categories"));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        log.debug("Rejected request to {}: {}", exchange.getRequest().getURI().getPath(), message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
