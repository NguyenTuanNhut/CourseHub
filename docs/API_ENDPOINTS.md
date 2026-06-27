# API Endpoints - Course Management and Project Grading System

All endpoints are prefixed with `/api/v1`.

---

## 1. Authentication (Public & Authenticated)

| Endpoint | Method | Role | Request Body | Description |
|---|---|---|---|---|
| `/auth/login` | POST | PUBLIC | `LoginRequest` | Authenticate user & return JWT tokens |
| `/auth/refresh` | POST | PUBLIC | `RefreshTokenRequest` | Rotate refresh token |
| `/auth/register/students` | POST | PUBLIC | `RegisterStudentRequest` | Public student registration |
| `/auth/forgot-password` | POST | PUBLIC | `ForgotPasswordRequest` | Request password reset instructions |
| `/auth/reset-password` | POST | PUBLIC | `ResetPasswordRequest` | Reset password using reset token |
| `/auth/logout` | POST | AUTHENTICATED | Header: `Bearer {accessToken}` | Logout user & blacklist tokens |
| `/auth/change-password` | POST | AUTHENTICATED | `ChangePasswordRequest` | Change user password |

---

## 2. Admin Management (Role: `ADMIN`)

| Endpoint | Method | Params / Request | Description |
|---|---|---|---|
| `/admin/users` | GET | `keyword`, `role`, `isActive`, pagination, sorting | List/search users |
| `/admin/users/{userId}` | GET | Path: user ID | Get user details |
| `/admin/users` | POST | `CreateUserRequest` | Create user (Lecturer, Admin, Student) |
| `/admin/users/{userId}` | PUT | `UpdateUserRequest` | Update user details |
| `/admin/users/{userId}/status` | PATCH | Param: `isActive` (boolean) | Toggle user active status |
| `/admin/users/{userId}` | DELETE | Path: user ID | Delete user account |
| `/admin/courses` | GET | `keyword`, `status`, pagination, sorting | List/search courses |
| `/admin/courses/{courseId}` | GET | Path: course ID | Get Course details |
| `/admin/courses` | POST | `CreateCourseRequest` | Create new course |
| `/admin/courses/{courseId}` | PUT | `CreateCourseRequest` | Update course details |
| `/admin/courses/{courseId}/status` | PATCH | Param: `status` (CourseStatus) | Update Course status |
| `/admin/courses/{courseId}/lecturer` | PATCH | Request: `{"lecturerId": 10}` | Assign Lecturer to course |
| `/admin/courses/{courseId}` | DELETE | Path: course ID | Delete course from system |

---

## 3. Lecturer Actions (Role: `LECTURER`)

| Endpoint | Method | Params / Request | Description |
|---|---|---|---|
| `/lecturer/courses` | GET | None | View assigned courses |
| `/lecturer/courses/{courseId}/assignments` | POST | `CreateAssignmentRequest` | Create course assignment |
| `/lecturer/courses/{courseId}/assignments` | GET | Path: course ID | Get course assignments |
| `/lecturer/assignments/{assignmentId}` | GET | Path: assignment ID | Get assignment details |
| `/lecturer/assignments/{assignmentId}` | PUT | `CreateAssignmentRequest` | Update assignment details |
| `/lecturer/assignments/{assignmentId}/status` | PATCH | Param: `status` (AssignmentStatus) | Update assignment status |
| `/lecturer/assignments/{assignmentId}` | DELETE | Path: assignment ID | Delete assignment (no submissions) |
| `/lecturer/courses/{courseId}/submissions` | GET | Path: course ID | View course student submissions |
| `/lecturer/assignments/{assignmentId}/submissions` | GET | Path: assignment ID | View assignment submissions |
| `/lecturer/submissions/{submissionId}` | GET | Path: submission ID | View specific submission details |
| `/lecturer/grades` | POST | `GradeSubmissionRequest` | Grade/feedback submission |
| `/lecturer/grades/{gradeId}` | PUT | `GradeSubmissionRequest` | Update grading/feedback |
| `/lecturer/courses/{courseId}/materials` | POST | Multipart: `title`, `description`, `file` | Upload lecture material |
| `/lecturer/courses/{courseId}/materials` | GET | Path: course ID | List course materials |
| `/lecturer/materials/{materialId}` | GET | Path: material ID | View specific material details |
| `/lecturer/materials/{materialId}` | DELETE | Path: material ID | Delete lecture material |

---

## 4. Student Actions (Role: `STUDENT`)

| Endpoint | Method | Params / Request | Description |
|---|---|---|---|
| `/student/courses` | GET | `keyword`, pagination | List/search available open courses |
| `/student/courses/{courseId}` | GET | Path: course ID | Get details of a course |
| `/student/courses/{courseId}/enrollments` | POST | Path: course ID | Enroll in course |
| `/student/enrollments` | GET | None | View enrolled courses |
| `/student/enrollments/{enrollmentId}` | GET | Path: enrollment ID | Get enrollment details |
| `/student/enrollments/{enrollmentId}` | DELETE | Path: enrollment ID | Cancel enrollment |
| `/student/courses/{courseId}/assignments` | GET | Path: course ID | List assignments in enrolled course |
| `/student/assignments/{assignmentId}` | GET | Path: assignment ID | View specific assignment |
| `/student/assignments/{assignmentId}/submissions` | POST | `SubmitAssignmentRequest` (GitHub URL) | Submit assignment via URL |
| `/student/submissions/upload` | POST | Multipart: `assignmentId`, `githubUrl`, `file` | Submit via report file & URL |
| `/student/submissions` | GET | None | View student's submissions |
| `/student/submissions/{submissionId}` | GET | Path: submission ID | View submission details |
| `/student/submissions/{submissionId}/grade` | GET | Path: submission ID | View score and feedback |
| `/student/courses/{courseId}/materials` | GET | Path: course ID | View course materials |
| `/student/materials/{materialId}` | GET | Path: material ID | Get details of a material |
