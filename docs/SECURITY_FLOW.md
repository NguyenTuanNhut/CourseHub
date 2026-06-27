# Security Flow - Course Management and Project Grading System

This monolithic backend implements stateless authentication and role-based access control (RBAC) via Spring Security, JWT (JSON Web Tokens), and a Redis-backed token revocation list.

---

## 1. Authentication & Token Issuance

1. **Client Request**: Client sends a POST to `/api/v1/auth/login` containing `username`/`email` and plain-text `password`.
2. **Verification**: `AuthenticationManager` uses BCrypt (strength 10) to verify passwords. Inactive accounts are blocked (403).
3. **Token Generation**: If valid, the server generates:
   - **Access Token**: Short-lived (e.g. 15-30 minutes). Contains `subject` (username), `userId`, `role` (e.g., `ROLE_STUDENT`), and a unique JTI (JWT ID).
   - **Refresh Token**: Long-lived (e.g. 7-30 days). Contains `subject`, `userId`, and a unique JTI.
4. **Token Persistence**:
   - Access token is stateless and is not saved on the server.
   - Refresh Token is hashed using BCrypt and saved as a `RefreshToken` entity in MySQL (linked to user, with its unique JTI stored).

---

## 2. Stateless JWT Authorization Filter

Each secured request is processed by `JwtAuthenticationFilter`:
1. Checks the `Authorization` header for `Bearer {accessToken}`.
2. Parses and validates the JWT signature and expiration.
3. Extracts JTI (token ID).
4. **Revocation Check**: Queries `RevokedTokenService` to check if JTI is blacklisted.
   - **Redis First**: Checks if `jwt:blacklist:{jti}` exists in Redis.
   - **MySQL Fallback**: If Redis fails/is offline, queries the MySQL `token_blacklist` table.
   - If blacklisted, returns **401 Unauthorized**.
5. **Context Population**: If valid, loads `UserDetails`, checks if the user is active, creates an `Authentication` token, and registers it in `SecurityContextHolder`.

---

## 3. Refresh Token Rotation (RTR) & Reuse Detection

To prevent token theft and replay attacks, we implement Refresh Token Rotation:
1. Client calls `/api/v1/auth/refresh` sending a refresh token.
2. Server validates signature/expiration and JTI.
3. Retrieves the `RefreshToken` entity matching the JTI from the database.
4. **State Check**:
   - **Already Revoked/Reused**: If the refresh token has already been marked as `revoked = true` or contains a `replaced_by_token_id`, **Token Reuse** is detected. Server immediately revokes all active sessions (Refresh Tokens) for that user and rejects the request with **401 Unauthorized**.
   - **Valid**: Server marks the old Refresh Token as `revoked = true` and updates `replacedByTokenId` to point to the new token.
5. **Rotation**: Server issues a brand new Access Token and a brand new Refresh Token. Persists the new Refresh Token in the database.

---

## 4. Logout & Blacklisting Flow

1. Client calls POST `/api/v1/auth/logout` with Access Token in the Authorization header.
2. Server extracts Access Token, parses JTI, and retrieves the remaining expiration duration.
3. Server calls `RevokedTokenService.revoke(jti, expiration)`:
   - Sets key `jwt:blacklist:{jti}` in Redis with TTL equal to the token's remaining lifetime.
   - As a persistent fallback, also saves a row into MySQL `token_blacklist` table.
4. Server marks all active Refresh Tokens for that user as `revoked = true` in MySQL.
5. Returns **200 OK**. Future requests using the JTI are blocked until the token naturally expires.

---

## 5. Fail-Closed Blacklist Fallback

If the Redis connection fails, the application does NOT fail-open (meaning it won't silently accept blacklisted tokens). 
It falls back to querying the MySQL `token_blacklist` table to verify JTI status. If both Redis and MySQL query fails, it defaults to a fail-closed status and rejects the request.
