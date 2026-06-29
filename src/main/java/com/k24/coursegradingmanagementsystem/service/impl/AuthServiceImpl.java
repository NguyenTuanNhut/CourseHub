package com.k24.coursegradingmanagementsystem.service.impl;

import com.k24.coursegradingmanagementsystem.dto.request.*;
import com.k24.coursegradingmanagementsystem.dto.response.AuthResponse;
import com.k24.coursegradingmanagementsystem.dto.response.UserResponse;
import com.k24.coursegradingmanagementsystem.entity.PasswordResetToken;
import com.k24.coursegradingmanagementsystem.entity.RefreshToken;
import com.k24.coursegradingmanagementsystem.entity.User;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.exception.BusinessRuleException;
import com.k24.coursegradingmanagementsystem.exception.ConflictException;
import com.k24.coursegradingmanagementsystem.exception.InvalidTokenException;
import com.k24.coursegradingmanagementsystem.exception.ResourceNotFoundException;
import com.k24.coursegradingmanagementsystem.mapper.UserMapper;
import com.k24.coursegradingmanagementsystem.repository.PasswordResetTokenRepository;
import com.k24.coursegradingmanagementsystem.repository.RefreshTokenRepository;
import com.k24.coursegradingmanagementsystem.repository.UserRepository;
import com.k24.coursegradingmanagementsystem.security.CustomUserDetails;
import com.k24.coursegradingmanagementsystem.security.JwtProvider;
import com.k24.coursegradingmanagementsystem.service.AuthService;
import com.k24.coursegradingmanagementsystem.service.token.RevokedTokenService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RevokedTokenService revokedTokenService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           RevokedTokenService revokedTokenService,
                           JwtProvider jwtProvider,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.revokedTokenService = revokedTokenService;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (!user.getIsActive()) {
            throw new BusinessRuleException("User account is locked or deactivated");
        }

        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name(), accessJti);
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getUsername(), refreshJti);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .tokenId(refreshJti)
                .tokenHash(passwordEncoder.encode(refreshToken))
                .expiresAt(Instant.now().plusMillis(jwtProvider.getRefreshExpirationMs()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtProvider.getAccessExpirationMs() / 1000)
                .refreshTokenExpiresIn(jwtProvider.getRefreshExpirationMs() / 1000)
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtProvider.validateToken(token)) {
            throw new InvalidTokenException("Invalid refresh token signature or expiration");
        }

        Claims claims = jwtProvider.getClaimsFromToken(token);
        if (claims == null) {
            throw new InvalidTokenException("Could not parse refresh token claims");
        }

        String jti = claims.getId();
        Long userId = claims.get("userId", Long.class);

        RefreshToken storedToken = refreshTokenRepository.findByTokenId(jti)
                .orElseThrow(() -> {
                    // Token not found in DB - might be malicious or reuse of deleted token
                    revokeAllRefreshTokensForUser(userId);
                    return new InvalidTokenException("Refresh token is unrecognized. Revoking all tokens for security.");
                });

        if (storedToken.getRevoked()) {
            // Reuse detected! Someone is trying to re-use an old refresh token.
            // Revoke all tokens for this user for security.
            revokeAllRefreshTokensForUser(userId);
            throw new InvalidTokenException("Refresh token has already been rotated. Suspicious activity detected. All active sessions revoked.");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token has expired. Please login again.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getIsActive()) {
            throw new BusinessRuleException("User account is locked or deactivated");
        }

        // Rotate Refresh Token: revoke the old one and generate a replacement pair
        String newAccessJti = UUID.randomUUID().toString();
        String newRefreshJti = UUID.randomUUID().toString();

        storedToken.setRevoked(true);
        storedToken.setReplacedByTokenId(newRefreshJti);
        refreshTokenRepository.save(storedToken);

        String newAccessToken = jwtProvider.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name(), newAccessJti);
        String newRefreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getUsername(), newRefreshJti);

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .tokenId(newRefreshJti)
                .tokenHash(passwordEncoder.encode(newRefreshToken))
                .expiresAt(Instant.now().plusMillis(jwtProvider.getRefreshExpirationMs()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresIn(jwtProvider.getAccessExpirationMs() / 1000)
                .refreshTokenExpiresIn(jwtProvider.getRefreshExpirationMs() / 1000)
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authHeader.substring(7);
        if (jwtProvider.validateToken(token)) {
            String jti = jwtProvider.getJtiFromToken(token);
            Claims claims = jwtProvider.getClaimsFromToken(token);
            if (claims != null) {
                Instant expiration = claims.getExpiration().toInstant();
                Long userId = claims.get("userId", Long.class);

                // Blacklist the access token
                revokedTokenService.revoke(jti, expiration);

                // Revoke all active refresh tokens for the logout session
                revokeAllRefreshTokensForUser(userId);
                log.info("User {} logged out successfully. Access Token JTI {} blacklisted.", userId, jti);
            }
        }
    }

    @Override
    public UserResponse registerStudent(RegisterStudentRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessRuleException("Password and confirm password do not match");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank() && userRepository.existsByPhone(request.getPhone())) {
            throw new ConflictException("Phone number is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(Role.STUDENT)
                .isActive(true)
                .build();

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BusinessRuleException("New password and confirmation do not match");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("Current password is incorrect");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BusinessRuleException("New password must be different from the old password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke active sessions
        revokeAllRefreshTokensForUser(user.getId());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // ALWAYS return a generic message to prevent email discovery attacks
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            String tokenHash = passwordEncoder.encode(token);

            // Set expiration to 30 minutes from now
            Instant expiration = Instant.now().plus(30, ChronoUnit.MINUTES);

            // Invalidate any existing unused reset tokens for this user first
            List<PasswordResetToken> existing = passwordResetTokenRepository.findByUserIdAndUsedFalse(user.getId());
            existing.forEach(t -> {
                t.setUsed(true);
                passwordResetTokenRepository.save(t);
            });

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .userId(user.getId())
                    .tokenHash(tokenHash)
                    .expiresAt(expiration)
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            // Simulated action (never log raw token inside real logs, but we output to standard stdout for demonstration)
            System.out.println("[SIMULATED EMAIL] Password reset token generated for: " + user.getEmail() + " | Token: " + token);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BusinessRuleException("New password and confirmation do not match");
        }

        // We must hash user-provided token to match the stored hash, or search/validate
        // Since we stored tokenHash as passwordEncoder.encode(rawToken), how do we retrieve it?
        // Ah! If we use passwordEncoder.encode, it generates random salt every time, so we cannot search by matching hash directly.
        // It is better to use plain token UUID inside the database, or use a secure hash (e.g. SHA-256) instead of BCrypt.
        // Let's check: if we search through all active/unused reset tokens and verify via matches, that is secure.
        // To optimize, let's load all unused reset tokens and check them.
        List<PasswordResetToken> tokens = passwordResetTokenRepository.findAll().stream()
                .filter(t -> !t.getUsed() && t.getExpiresAt().isAfter(Instant.now()))
                .toList();

        PasswordResetToken validToken = null;
        for (PasswordResetToken t : tokens) {
            if (passwordEncoder.matches(request.getToken(), t.getTokenHash())) {
                validToken = t;
                break;
            }
        }

        if (validToken == null) {
            throw new InvalidTokenException("Invalid or expired password reset token");
        }

        User user = userRepository.findById(validToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        validToken.setUsed(true);
        passwordResetTokenRepository.save(validToken);

        // Revoke all refresh tokens
        revokeAllRefreshTokensForUser(user.getId());
        log.info("Password successfully reset for user id: {}", user.getId());
    }

    private void revokeAllRefreshTokensForUser(Long userId) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
        activeTokens.forEach(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }
}
