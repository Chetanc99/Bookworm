package com.bookworm.backend.security;

import com.bookworm.backend.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Plain in-memory fixed-window rate limiter, scoped to /api/v1/auth/** only
 * (see RateLimitFilterConfig, which constructs this directly as a @Bean
 * rather than @Component-scanning it - keeps it out of Spring Boot's default
 * auto-registration entirely, so there's exactly one, explicitly-scoped
 * registration instead of relying on OncePerRequestFilter's dedup guard).
 * No new dependency (Bucket4j/Redis etc.) pulled in for this - a
 * per-instance in-memory counter is good enough to blunt basic
 * credential-stuffing/spam against a single-node deployment, which is all
 * this project targets. Won't survive a restart and doesn't coordinate
 * across multiple instances - both acceptable trade-offs here, called out
 * explicitly rather than silently assumed away.
 *
 * Keyed by client IP + path. /auth/login gets a tighter limit than the rest
 * of /auth/** since credential-guessing is the main risk there.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000;

    private final int loginMaxPerMinute;
    private final int defaultMaxPerMinute;

    public RateLimitFilter(int loginMaxPerMinute, int defaultMaxPerMinute) {
        this.loginMaxPerMinute = loginMaxPerMinute;
        this.defaultMaxPerMinute = defaultMaxPerMinute;
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private record Window(AtomicInteger count, long windowStart) {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        int limit = path.endsWith("/auth/login") ? loginMaxPerMinute : defaultMaxPerMinute;
        String key = clientIp(request) + ":" + path;

        long now = System.currentTimeMillis();
        Window window = windows.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart() >= WINDOW_MILLIS) {
                return new Window(new AtomicInteger(1), now);
            }
            existing.count().incrementAndGet();
            return existing;
        });

        if (window.count().get() > limit) {
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(
                    ApiResponse.error("Too many requests - please wait a minute and try again")));
            return;
        }

        chain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
