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
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, BucketState> buckets = new ConcurrentHashMap<>();

    @Value("${security.trust-proxy:false}")
    private boolean trustProxy;

    @Value("${security.rate-limit.max-attempts:5}")
    private long maxAttempts;

    @Value("${security.rate-limit.window-seconds:60}")
    private long windowSeconds;

    @Value("${security.rate-limit.cache-ttl-minutes:10}")
    private long cacheTtlMinutes;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        cleanupExpiredBuckets();

        String ip = resolverIp(request);
        BucketState state = buckets.compute(ip, (key, current) -> {
            if (current == null || current.isExpired(cacheTtlMinutes)) {
                return new BucketState(criarBucketParaIp());
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
        response.getWriter().write("{\"error\":\"Muitas tentativas. Tente novamente em 1 minuto.\"}");
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String servletPath = request.getServletPath();
        if (servletPath != null && !servletPath.isBlank()) {
            return "/auth/login".equals(servletPath);
        }

        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (requestUri == null) {
            return false;
        }

        String normalizedPath = requestUri;
        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            normalizedPath = requestUri.substring(contextPath.length());
        }

        return "/auth/login".equals(normalizedPath);
    }

    private Bucket criarBucketParaIp() {
        Bandwidth limite = Bandwidth.classic(maxAttempts, Refill.intervally(maxAttempts, Duration.ofSeconds(windowSeconds)));
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
        buckets.entrySet().removeIf(entry -> entry.getValue().isExpired(cacheTtlMinutes));
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
