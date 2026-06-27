# Test Report - Course Management and Project Grading System

## Execution Metrics

- **Total Tests Run**: 16
- **Service Unit Tests**: 10
- **Controller Endpoint Tests**: 6
- **Passed Tests**: 16
- **Failed Tests**: 0
- **Build / Verification Command**: `./gradlew clean test`

---

## Detailed Test Cases

### 1. Service Layer Tests

| Class | Method | Focus | Status |
|---|---|---|---|
| `AuthServiceTest` | `registerStudent_shouldCreateStudent_whenRequestIsValid` | Standard Student registration | PASSED |
| `AuthServiceTest` | `registerStudent_shouldThrowConflictException_whenEmailExists` | Duplicate email rejection (409) | PASSED |
| `EnrollmentServiceTest` | `enrollCourse_shouldCreateEnrollment_whenCourseIsAvailable` | Standard course enrollment | PASSED |
| `EnrollmentServiceTest` | `enrollCourse_shouldThrowConflictException_whenAlreadyEnrolled` | Duplicate enrollment check | PASSED |
| `SubmissionServiceTest` | `submitAssignment_shouldCreateSubmission_whenStudentIsEnrolled` | Standard Github URL submission | PASSED |
| `SubmissionServiceTest` | `submitAssignment_shouldThrowForbiddenException_whenStudentIsNotEnrolled` | Block non-enrolled students | PASSED |
| `GradeServiceTest` | `gradeSubmission_shouldSaveGradeAndMarkSubmissionAsGraded` | Standard submission grading | PASSED |
| `GradeServiceTest` | `gradeSubmission_shouldThrowInvalidStateException_whenSubmissionIsPending` | Block grading pending submissions | PASSED |
| `GradeServiceTest` | `gradeSubmission_shouldThrowInvalidGradeException_whenScoreIsInvalid` | Reject scores outside 0-100 bounds | PASSED |
| `GradeServiceTest` | `gradeSubmission_shouldThrowForbiddenException_whenLecturerDoesNotOwnCourse` | Block cross-lecturer grading | PASSED |

### 2. Controller/Endpoint Tests

| Class | Method | Focus | Status |
|---|---|---|---|
| `AuthControllerTest` | `login_shouldReturn200AndTokens_whenCredentialsAreValid` | Login returns 200/JWT | PASSED |
| `AuthControllerTest` | `registerStudent_shouldReturn400_whenRequestIsInvalid` | Registration schema validations | PASSED |
| `StudentCourseControllerTest` | `enrollCourse_shouldReturn201_whenSuccessful` | Course enrollment returns 201 | PASSED |
| `StudentCourseControllerTest` | `enrollCourse_shouldReturn409_whenAlreadyEnrolled` | Duplicate enrollment returns 409 | PASSED |
| `StudentCourseControllerTest` | `studentApi_shouldReturn401_whenTokenIsMissing` | Missing token returns 401 | PASSED |
| `LecturerCourseControllerTest` | `gradeSubmission_shouldReturn200_whenRequestIsValid` | Valid grading returns 200 | PASSED |
| `LecturerCourseControllerTest` | `gradeSubmission_shouldReturn400_whenScoreIsGreaterThan100` | Grading bounds constraint checks | PASSED |
| `LecturerCourseControllerTest` | `gradeSubmission_shouldReturn403_whenAccessedByStudent` | Student role blocked from Lecturer APIs | PASSED |

---

## Logging & Redis Revocation Verifications
- AOP aspect logs execution metrics for every request.
- Blacklist operations verify cache checks in Redis and fall back to the MySQL `token_blacklist` table if Redis connection exceptions are raised.
