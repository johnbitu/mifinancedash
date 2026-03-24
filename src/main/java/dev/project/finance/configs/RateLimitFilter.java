package dev.project.finance.configs;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, BucketState> buckets = new ConcurrentHashMap<>();

    @Value("${security.trust-proxy:false}")
    private boolean trustProxy;

    @Value("${security.rate-limit.login.max-attempts:5}")
    private long loginMaxAttempts = 5;

    @Value("${security.rate-limit.login.window-seconds:60}")
    private long loginWindowSeconds = 60;

    // Compatibilidade com testes antigos que ajustam esses campos por reflection.
    private long maxAttempts = 5;
    private long windowSeconds = 60;

    @Value("${security.rate-limit.register.max-attempts:3}")
    private long registerMaxAttempts = 3;

    @Value("${security.rate-limit.register.window-seconds:60}")
    private long registerWindowSeconds = 60;

    @Value("${security.rate-limit.refresh.max-attempts:10}")
    private long refreshMaxAttempts = 10;

    @Value("${security.rate-limit.refresh.window-seconds:60}")
    private long refreshWindowSeconds = 60;

    @Value("${security.rate-limit.cache-ttl-minutes:10}")
    private long cacheTtlMinutes;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        RateLimitTarget target = resolveTarget(request);
        if (target == null) {
            filterChain.doFilter(request, response);
            return;
        }

        cleanupExpiredBuckets();

        String ip = resolverIp(request);
        String key = target.path + ":" + ip;

        BucketState state = buckets.compute(key, (bucketKey, current) -> {
            if (current == null || current.isExpired(cacheTtlMinutes)) {
                return new BucketState(createBucket(target));
            }
            current.touch();
            return current;
        });

        if (state.bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"Muitas tentativas. Tente novamente em instantes.\"}");
    }

    private RateLimitTarget resolveTarget(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }

        String path = normalizePath(request);
        return switch (path) {
            case "/auth/login" -> new RateLimitTarget(path, resolveLoginAttempts(), resolveLoginWindowSeconds());
            case "/auth/register" -> new RateLimitTarget(path, registerMaxAttempts, registerWindowSeconds);
            case "/auth/refresh" -> new RateLimitTarget(path, refreshMaxAttempts, refreshWindowSeconds);
            default -> null;
        };
    }

    private long resolveLoginAttempts() {
        return maxAttempts != 5 ? maxAttempts : loginMaxAttempts;
    }

    private long resolveLoginWindowSeconds() {
        return windowSeconds != 60 ? windowSeconds : loginWindowSeconds;
    }

    private String normalizePath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath != null && !servletPath.isBlank()) {
            return servletPath;
        }

        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (requestUri == null) {
            return "";
        }

        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }

        return requestUri;
    }

    private Bucket createBucket(RateLimitTarget target) {
        Bandwidth limite = Bandwidth.classic(
                target.maxAttempts,
                Refill.intervally(target.maxAttempts, Duration.ofSeconds(target.windowSeconds))
        );
        return Bucket.builder().addLimit(limite).build();
    }

    private String resolverIp(HttpServletRequest request) {
        if (!trustProxy) {
            return request.getRemoteAddr();
        }

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void cleanupExpiredBuckets() {
        for (Map.Entry<String, BucketState> entry : buckets.entrySet()) {
            if (entry.getValue().isExpired(cacheTtlMinutes)) {
                buckets.remove(entry.getKey());
            }
        }
    }

    private record RateLimitTarget(String path, long maxAttempts, long windowSeconds) {
    }

    private static class BucketState {
        private final Bucket bucket;
        private Instant lastAccess;

        private BucketState(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccess = Instant.now();
        }

        private void touch() {
            this.lastAccess = Instant.now();
        }

        private boolean isExpired(long ttlMinutes) {
            return lastAccess.isBefore(Instant.now().minus(Duration.ofMinutes(ttlMinutes)));
        }
    }
}
