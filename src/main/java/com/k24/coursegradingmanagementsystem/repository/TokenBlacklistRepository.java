package com.k24.coursegradingmanagementsystem.repository;

import com.k24.coursegradingmanagementsystem.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    Optional<TokenBlacklist> findByTokenId(String tokenId);

    boolean existsByTokenId(String tokenId);
}
