# Requirement Checklist - Course Management and Project Grading System

| ID | Requirement | Status | Endpoint(s) | Controller | Service | Repository | Tests | Remaining Issues |
|---|---|---|---|---|---|---|---|---|
| **FR-01** | Login and JWT issuance | Complete | `/api/v1/auth/login` | `AuthController` | `AuthService` | `UserRepository` | `AuthControllerTest` | None |
| **FR-02** | Refresh Token rotation | Complete | `/api/v1/auth/refresh` | `AuthController` | `AuthService` | `RefreshTokenRepository` | `AuthControllerTest` | None |
| **FR-03** | Logout & Token revocation | Complete | `/api/v1/auth/logout` | `AuthController` | `AuthService`, `RevokedTokenService` | `TokenBlacklistRepository` | `AuthControllerTest` | None |
| **FR-04** | Student registration | Complete | `/api/v1/auth/register/students` | `AuthController` | `AuthService` | `UserRepository` | `AuthServiceTest` | None |
| **FR-05** | Admin User & Course management | Complete | `/api/v1/admin/users/**`, `/api/v1/admin/courses/**` | `AdminUserController`, `AdminCourseController` | `UserService`, `CourseService` | `UserRepository`, `CourseRepository` | `StudentCourseControllerTest` (access checks) | None |
| **FR-06** | Student course enrollment | Complete | `/api/v1/student/courses/{courseId}/enrollments` | `StudentCourseController` | `EnrollmentService` | `EnrollmentRepository` | `EnrollmentServiceTest`, `StudentCourseControllerTest` | None |
| **FR-07** | Assignment and project submission | Complete | `/api/v1/student/assignments/{assignmentId}/submissions`, `/api/v1/student/submissions/upload` | `StudentCourseController` | `SubmissionService` | `SubmissionRepository` | `SubmissionServiceTest` | None |
| **FR-08** | Lecturer grading and feedback | Complete | `/api/v1/lecturer/grades` | `LecturerCourseController` | `GradeService` | `GradeRepository` | `GradeServiceTest`, `LecturerCourseControllerTest` | None |
| **FR-09** | Lecture material upload | Complete | `/api/v1/lecturer/courses/{courseId}/materials` | `LecturerCourseController` | `LectureMaterialService`, `FileStorageService` | `LectureMaterialRepository` | `LecturerCourseControllerTest` | None |
| **FR-10** | Change and forgot password | Complete | `/api/v1/auth/change-password`, `/api/v1/auth/forgot-password`, `/api/v1/auth/reset-password` | `AuthController` | `AuthService` | `PasswordResetTokenRepository` | `AuthServiceTest` (indirectly) | None |
