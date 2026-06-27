package com.k24.coursegradingmanagementsystem.service.token;

import java.time.Instant;

public interface RevokedTokenService {

    void revoke(String tokenId, Instant expiration);

    boolean isRevoked(String tokenId);
}
