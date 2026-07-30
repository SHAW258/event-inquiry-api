# 🎯 Technical Discussion & Interview Preparation Guide
## Event Inquiry Management API

This guide provides structured talking points, design justifications, architectural explanations, code walkthroughs, and expected interview Q&A for your upcoming technical evaluation discussion.

---

## 📋 Quick Talking Points Summary (Elevator Pitch)

> *"The Event Inquiry Management API is a stateless Spring Boot 3 RESTful micro-service built with Java 17, Spring Security 6, Spring Data JPA, and PostgreSQL. It enforces role-based access control (USER vs ADMIN) and strict server-side resource ownership validation to eliminate Insecure Direct Object Reference (IDOR) vulnerabilities. The application follows clean layered architecture with DTO projections, unified exception handling, interactive OpenAPI specs, containerized Docker deployment, and complete automated test coverage."*

---

## 🏛️ Topic 1: API Architecture & Layered Design

### Key Talking Points:
- **Clean Layered Architecture**: Strictly separates HTTP contract (`Controller`), Security (`Filter/Provider`), Business Logic (`Service`), Data Access (`Repository`), and Persistence (`Entity`).
- **DTO Projection Pattern**: Internal JPA entities are **never** returned directly to API consumers. DTOs (`EventInquiryCreateRequest`, `EventInquiryResponse`) isolate database entity fields from the external API contract.
- **Unified Global Exception Handling**: Annotated with `@RestControllerAdvice`, intercepting validation errors (`MethodArgumentNotValidException`) and domain exceptions (`ResourceNotFoundException`, `UnauthorizedAccessException`) to return standard JSON error responses (`ApiErrorResponse`).
- **Pagination & Sorting**: Implemented on all list endpoints using Spring Data `Pageable` (`PageRequest.of(page, size, sort)`) wrapped in a generic `PagedResponse<T>` wrapper to prevent memory exhaustion on large datasets.

### Expected Questions & Model Answers:
> **Q: Why did you use DTOs instead of returning JPA Entities directly in the controller?**  
> **A**: Returning JPA entities directly causes circular reference issues during JSON serialization (e.g. `User` -> `EventInquiry` -> `User`), exposes sensitive fields like password hashes, leaks database schema changes to API clients, and forces tight coupling between database tables and API contracts. DTOs isolate API contracts from database schema evolution.

---

## 🗄️ Topic 2: Database Schema & JPA Entity Design

### Key Talking Points:
- **Relational Structure**:
  - `users` table: Holds account credentials, full names, password hashes, and assigned roles.
  - `event_inquiries` table: Holds inquiry details (`customer_name`, `event_type`, `event_date`, `location`, `estimated_budget`, `guest_count`, `status`).
  - **Relationship**: `1-to-Many` (`User 1 ──> N EventInquiry`). Mapped using `@ManyToOne(fetch = FetchType.LAZY)` with `user_id` foreign key.
- **Data Type Choices**:
  - `estimated_budget`: Uses `BigDecimal` mapped to PostgreSQL `NUMERIC(12,2)` to prevent floating-point rounding errors common with `double` or `float`.
  - Enums (`Role`, `EventType`, `InquiryStatus`): Mapped using `@Enumerated(EnumType.STRING)` so database entries are readable strings rather than brittle ordinal numbers.
- **Indexing Strategy**:
  - Primary Key indexes (`id`)
  - Unique index on `users.email` for fast O(1) user login lookup
  - Foreign key index `idx_inquiries_user_id` on `event_inquiries(user_id)` to speed up `findByUserId` queries
  - Filtering indexes `idx_inquiries_status` and `idx_inquiries_event_type` for administrative search queries

### Expected Questions & Model Answers:
> **Q: Why did you use `FetchType.LAZY` on the `User` association in `EventInquiry`?**  
> **A**: Default `ManyToOne` fetching in JPA is `EAGER`, which leads to N+1 query problems when fetching lists of inquiries. `LAZY` loading ensures that user records are only fetched when explicitly accessed, optimizing database query performance.

---

## 🔒 Topic 3: Authentication Flow & Security Architecture

### Key Talking Points:
- **Stateless Authentication**: Uses JWT (JSON Web Tokens) rather than server-side HTTP sessions, allowing horizontal scaling across multiple container instances without sticky sessions.
- **Password Protection**: Passwords are encrypted using **BCrypt** with salt before being stored in the database.
- **Filter Pipeline**:
  1. Client sends request with `Authorization: Bearer <token>`.
  2. `JwtAuthenticationFilter` intercepts the request before reaching controllers.
  3. `JwtTokenProvider` parses the signature using secret key, checks expiration date, and extracts subject (email) and roles.
  4. `CustomUserDetailsService` loads user claims and sets `UsernamePasswordAuthenticationToken` in `SecurityContextHolder`.

### Expected Questions & Model Answers:
> **Q: How are JWT tokens generated and validated?**  
> **A**: On login, `JwtTokenProvider` builds a token using HMAC SHA-256 containing the user's email as subject and assigned role as claim. On incoming requests, `JwtAuthenticationFilter` parses the token against the secret key using `Jwts.parser().verifyWith(key())`. If valid and non-expired, the security context is populated for that request thread.

---

## 🛡️ Topic 4: Security Decisions & IDOR Prevention

### Key Talking Points:
- **Role-Based Access Control (RBAC)**:
  - Public endpoints: `/api/v1/auth/register`, `/api/v1/auth/login`, `/swagger-ui/**`, `/actuator/health`.
  - Admin endpoints (`@PreAuthorize("hasRole('ADMIN')")`): `GET /api/v1/inquiries` (view all), `PATCH /api/v1/inquiries/{id}/status` (update status).
  - User/Owner endpoints: Creating and viewing own inquiries.
- **Insecure Direct Object Reference (IDOR) Prevention**:
  - IDOR happens when a user modifies an ID parameter (e.g. changing `/inquiries/1` to `/inquiries/2`) to view another user's private data.
  - **Backend Enforcement**: In `EventInquiryServiceImpl`, `verifyOwnershipOrAdmin` verifies that `inquiry.getUser().getId()` matches `currentUser.getId()`. If not matching and user is not `ADMIN`, it throws `UnauthorizedAccessException` returning `HTTP 403 Forbidden`.

### Expected Questions & Model Answers:
> **Q: How did you ensure one user cannot access another user's inquiry by changing the ID?**  
> **A**: Authorization is enforced directly in the service layer, not relying solely on client-side routing. When `getInquiryById(id, currentUserEmail)` is called, the service retrieves the inquiry from the database, fetches the authenticated user's ID, and asserts equality (`Objects.equals(inquiry.getUser().getId(), currentUser.getId())`). If the user is neither the owner nor an admin, an explicit `UnauthorizedAccessException` is thrown, returning `HTTP 403 Forbidden`.

---

## ⚙️ Topic 5: Implementation Details & Trade-offs

### Key Talking Points:
- **Multi-Profile Configuration**:
  - `dev` profile: Uses embedded H2 database (`jdbc:h2:mem:eventinquirydb`) for zero-setup instant local testing.
  - `docker` profile: Uses PostgreSQL 16 container (`jdbc:postgresql://postgres:5432/event_inquiry_db`).
  - `test` profile: Uses isolated H2 instance for running automated tests.
- **Containerization**: Multi-stage `Dockerfile` compiles Maven build in Stage 1 and runs lightweight JRE Alpine image in Stage 2 with dynamic `$PORT` binding for Railway/Cloud platforms.
- **Automated Testing**: 9 JUnit 5 tests covering context load, controller MockMvc endpoints, service logic, and `SecurityAuthorizationTest` verifying IDOR security checks.

---

## 💡 Cheatsheet: Quick Answers for Common Technical Interview Questions

| Question | Short Bullet Answer |
| :--- | :--- |
| **Why Spring Boot 3 & Java 17?** | Java 17 LTS provides enhanced performance and pattern matching; Spring Boot 3 provides Jakarta EE 10 namespace support, native Spring Security 6 lambda syntax, and improved GraalVM readiness. |
| **How is status workflow managed?** | `InquiryStatus` enum defines valid states (`PENDING`, `UNDER_REVIEW`, `CONFIRMED`, `REJECTED`, `CANCELLED`, `COMPLETED`). Status updates are restricted to `ROLE_ADMIN` via `PATCH /api/v1/inquiries/{id}/status`. |
| **How did you test IDOR prevention?** | Created `SecurityAuthorizationTest` with `@WithMockUser`. Mocked User 2 requesting User 1's inquiry ID and verified that MockMvc returned `status().isForbidden()`. |
| **How would you scale this application?** | 1) Add Redis for JWT blacklist/refresh token storage. 2) Deploy PostgreSQL read-replicas for `GET /inquiries` read scaling. 3) Use Kafka/RabbitMQ for event-driven asynchronous email notifications upon status changes. |

---

## 📁 File Reference Cheat Sheet
- **Service Security Logic**: [`EventInquiryServiceImpl.java`](file:///C:/Users/indrajit/Desktop/event-inquiry-api/src/main/java/com/eventmanagement/api/service/impl/EventInquiryServiceImpl.java)
- **Security Configuration**: [`SecurityConfig.java`](file:///C:/Users/indrajit/Desktop/event-inquiry-api/src/main/java/com/eventmanagement/api/config/SecurityConfig.java)
- **JWT Token Provider**: [`JwtTokenProvider.java`](file:///C:/Users/indrajit/Desktop/event-inquiry-api/src/main/java/com/eventmanagement/api/security/JwtTokenProvider.java)
- **IDOR Security Test**: [`SecurityAuthorizationTest.java`](file:///C:/Users/indrajit/Desktop/event-inquiry-api/src/test/java/com/eventmanagement/api/security/SecurityAuthorizationTest.java)
- **Global Error Handler**: [`GlobalExceptionHandler.java`](file:///C:/Users/indrajit/Desktop/event-inquiry-api/src/main/java/com/eventmanagement/api/exception/GlobalExceptionHandler.java)
