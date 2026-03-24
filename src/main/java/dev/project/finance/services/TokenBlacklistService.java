package dev.project.finance.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenBlacklistService {

    private final Cache<String, Instant> blacklist;

    public TokenBlacklistService(@Value("${security.blacklist.max-size:10000}") long maxSize) {
        this.blacklist = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .build();
    }

    public void revogar(String token, long ttlSeconds) {
        if (token == null || token.isBlank()) {
            return;
        }

        long ttl = Math.max(ttlSeconds, 1);
        blacklist.put(token, Instant.now().plus(Duration.ofSeconds(ttl)));
    }

    public boolean estaRevogado(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Instant expiraEm = blacklist.getIfPresent(token);
        if (expiraEm == null) {
            return false;
        }

        if (Instant.now().isAfter(expiraEm)) {
            blacklist.invalidate(token);
            return false;
        }

        return true;
    }
}
