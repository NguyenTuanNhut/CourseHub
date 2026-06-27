package com.k24.coursegradingmanagementsystem.service.token.impl;

import com.k24.coursegradingmanagementsystem.entity.TokenBlacklist;
import com.k24.coursegradingmanagementsystem.exception.InvalidTokenException;
import com.k24.coursegradingmanagementsystem.repository.TokenBlacklistRepository;
import com.k24.coursegradingmanagementsystem.service.token.RevokedTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
public class RevokedTokenServiceImpl implements RevokedTokenService {

    private final StringRedisTemplate redisTemplate;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public RevokedTokenServiceImpl(@org.springframework.beans.factory.annotation.Autowired(required = false) StringRedisTemplate redisTemplate,
                                   TokenBlacklistRepository tokenBlacklistRepository) {
        this.redisTemplate = redisTemplate;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Override
    public void revoke(String tokenId, Instant expiration) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new InvalidTokenException("Token ID is invalid");
        }

        Duration ttl = Duration.between(Instant.now(), expiration);
        if (ttl.isZero() || ttl.isNegative()) {
            // Already expired, but let's record it in database fallback anyway for safety
            saveToMysql(tokenId, expiration);
            return;
        }

        if (redisTemplate != null) {
            String key = "jwt:blacklist:" + tokenId;
            try {
                redisTemplate.opsForValue().set(key, "revoked", ttl);
                log.info("Token JTI {} successfully blacklisted in Redis with TTL {}s", tokenId, ttl.toSeconds());
            } catch (Exception e) {
                log.error("Redis failed to blacklist token {}. Falling back to database: {}", tokenId, e.getMessage());
            }
        } else {
            log.debug("Redis is disabled, saving blacklist token directly to database");
        }
        
        saveToMysql(tokenId, expiration);
    }

    @Override
    public boolean isRevoked(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return true;
        }

        if (redisTemplate != null) {
            String key = "jwt:blacklist:" + tokenId;
            try {
                Boolean hasKey = redisTemplate.hasKey(key);
                if (Boolean.TRUE.equals(hasKey)) {
                    return true;
                }
            } catch (Exception e) {
                log.error("Redis is unavailable, checking blacklist from database: {}", e.getMessage());
            }
        }

        // Check fallback database blacklist integrity
        return tokenBlacklistRepository.existsByTokenId(tokenId);
    }

    private void saveToMysql(String tokenId, Instant expiration) {
        try {
            if (!tokenBlacklistRepository.existsByTokenId(tokenId)) {
                TokenBlacklist blacklist = TokenBlacklist.builder()
                        .tokenId(tokenId)
                        .expiresAt(expiration)
                        .build();
                tokenBlacklistRepository.save(blacklist);
                log.info("Token JTI {} blacklisted in MySQL fallback database", tokenId);
            }
        } catch (Exception e) {
            log.error("Failed to persist token revocation in MySQL fallback: {}", e.getMessage());
            // In a real system, we'd throw or alert, but we fail closed or log
        }
    }
}
