# Course Management and Project Grading System

A monolithic stateless REST API backend built with **Spring Boot 3.x** and **Java 17**, providing functionalities for courses, enrollments, student submissions, assignment grading, and Redis token revocation list.

---

## Technology Stack
- **Java**: Version 17
- **Framework**: Spring Boot 3.2.5
- **Build Tool**: Gradle 9.5.1
- **Database**: MySQL 8.x
- **In-Memory Store**: Redis 7.x
- **Database Migration**: Flyway DB
- **API Documentation**: Springdoc OpenAPI / Swagger UI
- **JWT Provider**: JJWT (Java JWT) 0.12.5
- **AOP Logging**: Spring AOP
- **Testing**: JUnit 5, Mockito, MockMvc

---

## Project Structure & Architecture
The system strictly enforces a layered monolithic architecture:
```text
Controller (REST Endpoints & Validation)
    ↓
Service (Business Rules & Resource Validation)
    ↓
Repository (MySQL / Redis / Local File Storage)
```

Package overview:
- `aspect`: Execution-time AOP logging aspects
- `config`: Security configuration, Redis serializer configurations, OpenAPI configurations
- `controller`: Modular routes for Auth, Admin, Lecturer, and Student operations
- `dto`: Request, Response, and Common JSON layout schemas
- `entity`: JPA persistence database entity models
- `enums`: State lifecycles (Role, CourseStatus, EnrollmentStatus, SubmissionStatus)
- `exception`: Custom exceptions mapping to global handler error responses
- `mapper`: Plain Java entity-to-DTO conversion mappers
- `repository`: Spring Data JPA repositories
- `security`: JWT Provider filter chain and Custom UserDetails implementation
- `service`: Core services logic and file upload interfaces

---

## Environment Configuration
Copy the configuration from [.env.example](file:///.env.example) to `.env` or set these environment variables before booting the system:
```properties
# Database URL & Credentials
DB_URL=jdbc:mysql://localhost:3306/course_grading_db
DB_USERNAME=root
DB_PASSWORD=

# Redis URL & Credentials
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_TIMEOUT=2000ms

# JWT Configurations (Keys must be Base64 encoded or at least 256 bits)
JWT_SECRET=9a3f9e8a7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d8c7b6a5f4e3d2c1b0a
JWT_ACCESS_EXPIRATION_MS=900000     # 15 Minutes
JWT_REFRESH_EXPIRATION_MS=604800000 # 7 Days
```

---

## Setup & Running the Application

### 1. Database Setup
Ensure you have a local instance of MySQL running, and create the schema:
```sql
CREATE DATABASE course_grading_db;
```

### 2. Redis Setup
Ensure a local Redis server is active on port `6379`.

### 3. Build & Compile
To clean compile the project and execute all mock tests:
```bash
./gradlew clean test
```

### 4. Running the Server
Launch the Spring Boot application locally:
```bash
./gradlew bootRun
```
The server starts on port `8080` (prefixed with `/api/v1`).

---

## Swagger UI Documentation
Open the API documentation page in your browser while the server is active:
- **Swagger URL**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **V3 API docs (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Development / Manual Testing Accounts
When testing, use these default accounts (created by migrations or your Admin controller):

### 1. Admin
- **Role**: ADMIN
- **Username**: `admin`
- **Password**: `Admin@123`

### 2. Lecturer
- **Role**: LECTURER
- **Username**: `lecturer01`
- **Password**: `Lecturer@123`

### 3. Student
- **Role**: STUDENT
- **Username**: `student01`
- **Password**: `Student@123`

---

## Known Limitations
1. **Mock File Storage**: File uploads (submissions, lecture materials) are currently persisted to the local directory (`uploads/`) with simulated URL responses `/api/v1/files/**` rather than an external AWS S3 or Cloudinary SDK instance to facilitate offline local development.
2. **Standard Port Configuration**: The application runs on default port `8080` and database port `3306`. Customize ports in `application.properties` if they conflict with existing services.
