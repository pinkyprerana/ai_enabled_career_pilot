package com.example.learningSpringBootApp.security;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token, Date expirationDate) {
        if (token != null && expirationDate != null) {
            blacklistedTokens.put(token, expirationDate.toInstant());
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null) {
            return false;
        }
        Instant expiry = blacklistedTokens.get(token);
        if (expiry == null) {
            return false;
        }
        if (expiry.isBefore(Instant.now())) {
            blacklistedTokens.remove(token);
            return false;
        }
        return true;
    }
}
