# Database Design - Course Management and Project Grading System

## Database Engine
- **Database**: MySQL 8.0+
- **Database Name**: `course_grading_db`

---

## Entity Relationship Details

### 1. Users (`users`)
Stores details for administrators, lecturers, and students.
- `id` (BIGINT, Primary Key, Auto Increment)
- `username` (VARCHAR(50), NOT NULL, Unique)
- `email` (VARCHAR(100), NOT NULL, Unique)
- `password_hash` (VARCHAR(100), NOT NULL)
- `full_name` (VARCHAR(100), NOT NULL)
- `phone` (VARCHAR(20))
- `role` (VARCHAR(20), NOT NULL) - `ADMIN`, `LECTURER`, `STUDENT`
- `is_active` (BOOLEAN, NOT NULL, Default: TRUE)
- `created_at` (TIMESTAMP, NOT NULL, Default: CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)

### 2. Courses (`courses`)
- `id` (BIGINT, Primary Key, Auto Increment)
- `course_code` (VARCHAR(30), NOT NULL, Unique)
- `course_name` (VARCHAR(150), NOT NULL)
- `description` (TEXT)
- `credit` (INT, NOT NULL)
- `lecturer_id` (BIGINT, Foreign Key to `users.id`)
- `maximum_students` (INT, NOT NULL)
- `start_date` (DATE, NOT NULL)
- `end_date` (DATE, NOT NULL)
- `status` (VARCHAR(30), NOT NULL, Default: `DRAFT`) - `DRAFT`, `OPEN`, `IN_PROGRESS`, `COMPLETED`, `CLOSED`
- `created_at` (TIMESTAMP, NOT NULL, Default: CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)

### 3. Enrollments (`enrollments`)
Matches students to courses they enroll in.
- `id` (BIGINT, Primary Key, Auto Increment)
- `course_id` (BIGINT, Foreign Key to `courses.id`)
- `student_id` (BIGINT, Foreign Key to `users.id`)
- `status` (VARCHAR(30), NOT NULL, Default: `ENROLLED`) - `ENROLLED`, `CANCELLED`, `COMPLETED`
- `enrolled_at` (TIMESTAMP, NOT NULL, Default: CURRENT_TIMESTAMP)
- `created_at` (TIMESTAMP, NOT NULL, Default: CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)
- **Unique Constraint**: `uq_course_student (course_id, student_id)`

### 4. Assignments (`assignments`)
Created by lecturers for courses.
- `id` (BIGINT, Primary Key, Auto Increment)
- `course_id` (BIGINT, Foreign Key to `courses.id`)
- `title` (VARCHAR(150), NOT NULL)
- `description` (TEXT)
- `instructions` (TEXT)
- `maximum_score` (DOUBLE, NOT NULL, Default: 100.0)
- `open_at` (TIMESTAMP, NOT NULL)
- `due_at` (TIMESTAMP, NOT NULL)
- `allow_late_submission` (BOOLEAN, NOT NULL, Default: FALSE)
- `status` (VARCHAR(30), NOT NULL, Default: `DRAFT`) - `DRAFT`, `OPEN`, `CLOSED`
- `created_by` (BIGINT, Foreign Key to `users.id`)
- `created_at` (TIMESTAMP, NOT NULL, Default: CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)

### 5. Submissions (`submissions`)
Student attempts for assignments.
- `id` (BIGINT, Primary Key, Auto Increment)
- `assignment_id` (BIGINT, Foreign Key to `assignments.id`)
- `student_id` (BIGINT, Foreign Key to `users.id`)
- `github_url` (VARCHAR(255))
- `report_url` (VARCHAR(255))
- `original_file_name` (VARCHAR(255))
- `file_type` (VARCHAR(50))
- `status` (VARCHAR(30), NOT NULL, Default: `PENDING`) - `PENDING`, `SUBMITTED`, `LATE`, `GRADED`
- `submitted_at` (TIMESTAMP, Nullable)
- `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)
- **Unique Constraint**: `uq_assignment_student (assignment_id, student_id)`

### 6. Grades (`grades`)
Stores scoring and feedback.
- `id` (BIGINT, Primary Key, Auto Increment)
- `submission_id` (BIGINT, Foreign Key to `submissions.id`, Unique)
- `lecturer_id` (BIGINT, Foreign Key to `users.id`)
- `score` (DOUBLE, NOT NULL)
- `feedback` (TEXT)
- `graded_at` (TIMESTAMP, NOT NULL, Default: CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)

### 7. Lecture Materials (`lecture_materials`)
- `id` (BIGINT, Primary Key, Auto Increment)
- `course_id` (BIGINT, Foreign Key to `courses.id`)
- `lecturer_id` (BIGINT, Foreign Key to `users.id`)
- `title` (VARCHAR(150), NOT NULL)
- `description` (TEXT)
- `file_url` (VARCHAR(255), NOT NULL)
- `original_file_name` (VARCHAR(255), NOT NULL)
- `file_type` (VARCHAR(50), NOT NULL)
- `file_size` (BIGINT, NOT NULL)
- `created_at` (TIMESTAMP, NOT NULL, Default: CURRENT_TIMESTAMP)
- `updated_at` (TIMESTAMP, Default: CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)

### 8. Refresh Tokens (`refresh_tokens`)
- `id` (BIGINT, Primary Key, Auto Increment)
- `user_id` (BIGINT, Foreign Key to `users.id`)
- `token_id` (VARCHAR(100), NOT NULL, Unique)
- `token_hash` (VARCHAR(100), NOT NULL)
- `expires_at` (TIMESTAMP, NOT NULL)
- `revoked` (BOOLEAN, NOT NULL, Default: FALSE)
- `replaced_by_token_id` (VARCHAR(100))
- `created_at` (TIMESTAMP, NOT NULL, Default: CURRENT_TIMESTAMP)

### 9. Token Blacklist (`token_blacklist`)
Persistent database fallback for revoked access tokens.
- `id` (BIGINT, Primary Key, Auto Increment)
- `user_id` (BIGINT, Foreign Key to `users.id`)
- `token_id` (VARCHAR(100), NOT NULL, Unique)
- `revoked_at` (TIMESTAMP, NOT NULL, Default: CURRENT_TIMESTAMP)
- `expires_at` (TIMESTAMP, NOT NULL)

### 10. Password Reset Tokens (`password_reset_tokens`)
- `id` (BIGINT, Primary Key, Auto Increment)
- `user_id` (BIGINT, Foreign Key to `users.id`)
- `token_hash` (VARCHAR(100), NOT NULL, Unique)
- `expires_at` (TIMESTAMP, NOT NULL)
- `used` (BOOLEAN, NOT NULL, Default: FALSE)
- `created_at` (TIMESTAMP, NOT NULL, Default: CURRENT_TIMESTAMP)

---

## Design Choices & Rationale
1. **Grade Isolation**: We maintain a separate `grades` table matching `submissions` on a 1-to-0..1 relationship. Having grades separated from submissions avoids duplicate columns, keeps the submission workflow separate from the evaluation workflow, and aligns with standard database normalization rules.
2. **Indexing Strategy**: Secondary indexes are applied to search filters like `users.username`, `users.role`, `courses.course_code`, and foreign keys (`student_id`, `course_id`) to ensure sub-second response times for paginated lookup queries.
