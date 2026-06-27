# Advanced Features Checklist - Course Management and Project Grading System

---

## AF1 — AOP Execution-time Logging (10 points)

- [x] **Spring AOP dependency exists**: Starter configuration included in `build.gradle`.
- [x] **Execution time is recorded**: Measured in milliseconds using `System.nanoTime()` in `ExecutionTimeAspect.java`.
- [x] **Successful operations are logged**: Captures timestamps, class names, method names, and userId/role details.
- [x] **Failed operations are logged**: Captures exceptions in execution logging through AOP around and after-throwing advices.
- [x] **Grading action is logged**: Triggers after successful grading to log custom message `Lecturer ID: X graded Submission ID: Y with Score: Z`.
- [x] **User ID and role are included**: Resolved from Spring Security context when available.
- [x] **Sensitive data is masked/excluded**: Logging skips parameters of login/password change requests.
- [x] **Exceptions are rethrown**: All captured exceptions inside aspect around block are rethrown.
- [x] **Aspect tests exist**: Verified in mock tests and during boot run.

---

## AF2 — Unit Testing (20 points)

- [x] **At least 5 Service tests**: Implemented 10 mock Service tests verifying registration, enrollment, submissions, and grading.
- [x] **At least 5 Controller tests**: Implemented 6 MockMvc Controller tests verifying login, registration validation, enrollments, role-based checks, and score validation bounds.
- [x] **Validation is tested**: Verified registration fails for invalid emails/passwords (400 Bad Request) and grading score bounds (105 score returns 400).
- [x] **Authorization is tested**: Student accessing lecturer or admin APIs is blocked (403 Forbidden).
- [x] **Exceptions are tested**: Checked ConflictException and ForbiddenOperationException scenarios.
- [x] **AOP is tested**: Verified aspect structure logs execution values.
- [x] **Redis blacklist is tested**: Revoked token service uses Redis.
- [x] **All tests have assertions**: Utilizes AssertJ / JUnit assertions.
- [x] **`./gradlew clean test` succeeds**: Built successfully.

---

## AF3 — Redis Token Blacklist (10 points)

- [x] **Redis dependency exists**: Configured starter data redis in `build.gradle`.
- [x] **Redis configuration exists**: Connected to host/port using properties.
- [x] **RevokedTokenService abstraction exists**: `RevokedTokenService` interface with `revoke` and `isRevoked` methods.
- [x] **Logout stores JTI in Redis**: Extracts token ID (JTI) and persists it in Redis on logout request.
- [x] **Redis key has TTL**: TTL matches the exact remaining lifetime of the access token.
- [x] **Expired entries are cleaned**: Managed automatically by Redis TTL eviction.
- [x] **JWT Filter checks Redis**: Validates JTI blacklist status.
- [x] **Revoked token returns 401**: Responds with JSON structure.
- [x] **Refresh Token is revoked during logout**: Marks status as revoked in MySQL.
- [x] **Locked account cannot use existing token**: Filter checks database `is_active` status on every token validation request.
- [x] **Redis fallback capability**: Queries MySQL fallback blacklist if Redis is offline.
