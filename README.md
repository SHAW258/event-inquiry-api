# 🎪 Event Inquiry Management API

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3.2.5](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![JWT Security](https://img.shields.io/badge/Spring%20Security-JWT-red.svg)](https://jwt.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

An enterprise-grade RESTful backend application built with **Java 17**, **Spring Boot 3**, **Spring Security**, **JPA/Hibernate**, and **PostgreSQL**. Designed for high performance, robust security, strict **Insecure Direct Object Reference (IDOR)** prevention, and seamless client integration.

---

## 📋 Table of Contents
- [Architecture Overview](#-architecture-overview)
- [Database Schema & ER Model](#-database-schema--er-model)
- [Security & Authorization Architecture](#-security--authorization-architecture)
- [API Endpoints Reference](#-api-endpoints-reference)
- [Prerequisites & Getting Started](#-prerequisites--getting-started)
- [Running with Docker Compose](#-running-with-docker-compose)
- [Running Tests](#-running-tests)
- [API Documentation & Postman Collection](#-api-documentation--postman-collection)
- [Pre-configured Seed Credentials](#-pre-configured-seed-credentials)

---

## 🏗️ Architecture Overview

The system follows a standard **Clean Layered Architecture** with strict boundary separation:

```
                          ┌─────────────────────────────┐
                          │   HTTP Clients / Postman    │
                          └──────────────┬──────────────┘
                                         │ JSON / HTTP
                                         ▼
                          ┌─────────────────────────────┐
                          │     REST Controllers        │
                          │ (Validation & OpenAPI Spec) │
                          └──────────────┬──────────────┘
                                         │
                        Security Context & JWT Authentication
                                         │
                                         ▼
                          ┌─────────────────────────────┐
                          │     Service Layer           │
                          │ (IDOR Checks, Business Rule)│
                          └──────────────┬──────────────┘
                                         │
                                         ▼
                          ┌─────────────────────────────┐
                          │   Spring Data JPA Repos     │
                          └──────────────┬──────────────┘
                                         │
                                         ▼
                          ┌─────────────────────────────┐
                          │   PostgreSQL / H2 Database  │
                          └─────────────────────────────┘
```

### Core Architecture Highlights:
- **Stateless Authentication**: JWT tokens issued upon successful authentication, verified on every request using a custom Spring Security filter (`JwtAuthenticationFilter`).
- **Strict Authorization**: Declarative method-level security (`@PreAuthorize`) paired with programmatic ownership validation in the service layer to eliminate IDOR vulnerabilities.
- **Unified Global Exception Handling**: Standardized JSON error response format (`ApiErrorResponse`) across all HTTP status codes (`400`, `401`, `403`, `404`, `409`, `500`).
- **DTO Projection Pattern**: Internal JPA entities are decoupled from external request/response contracts.

---

## 🗄️ Database Schema & ER Model

```
┌──────────────────────────────────────────┐       1 : N       ┌──────────────────────────────────────────┐
│                  users                   │ ────────────────> │             event_inquiries              │
├──────────────────────────────────────────┤                   ├──────────────────────────────────────────┤
│ id           : BIGINT (PK)               │                   │ id               : BIGINT (PK)           │
│ full_name    : VARCHAR(100) NOT NULL     │                   │ user_id          : BIGINT (FK -> users)   │
│ email        : VARCHAR(255) UNIQUE NOT N │                   │ customer_name    : VARCHAR(100) NOT NULL │
│ password     : VARCHAR(255) NOT NULL     │                   │ customer_email   : VARCHAR(255) NOT NULL │
│ role         : VARCHAR(20) NOT NULL      │                   │ customer_phone   : VARCHAR(30) NOT NULL  │
│ created_at   : TIMESTAMP NOT NULL        │                   │ event_type       : VARCHAR(30) NOT NULL  │
│ updated_at   : TIMESTAMP                 │                   │ event_date       : DATE NOT NULL         │
└──────────────────────────────────────────┘                   │ location         : VARCHAR(255) NOT NULL │
                                                               │ estimated_budget : NUMERIC(12,2) NOT N   │
                                                               │ guest_count      : INT NOT NULL          │
                                                               │ status           : VARCHAR(20) NOT NULL  │
                                                               │ special_requests : VARCHAR(1000)        │
                                                               │ created_at       : TIMESTAMP NOT NULL    │
                                                               │ updated_at       : TIMESTAMP             │
                                                               └──────────────────────────────────────────┘
```

### Field Definitions & Enums
- **User Roles (`Role`)**:
  - `ROLE_USER`: Standard customer user. Can create and view/manage only their own event inquiries.
  - `ROLE_ADMIN`: Administrative officer. Can view, update status, and manage all inquiries across the system.
- **Event Types (`EventType`)**:
  - `WEDDING`, `CORPORATE`, `BIRTHDAY`, `CONFERENCE`, `CONCERT`, `ANNIVERSARY`, `OTHER`.
- **Inquiry Statuses (`InquiryStatus`)**:
  - `PENDING`: Initial inquiry submitted by customer.
  - `UNDER_REVIEW`: Admin/team is processing the request.
  - `CONFIRMED`: Date & venue booking locked.
  - `REJECTED`: Request declined by management.
  - `CANCELLED`: Cancelled by customer/admin.
  - `COMPLETED`: Event successfully hosted.

---

## 🔒 Security & Authorization Architecture

### 1. Authentication Flow
1. **User Registration** (`POST /api/v1/auth/register`): Hashes plaintext passwords using **BCrypt** with salt before persistence.
2. **User Login** (`POST /api/v1/auth/login`): Validates email & password, returning a signed HMAC SHA-256 JWT Bearer token valid for 24 hours.
3. **Filter Pipeline**: `JwtAuthenticationFilter` intercepts requests, extracts the JWT from the `Authorization: Bearer <token>` header, parses claims, and populates the `SecurityContextHolder`.

### 2. IDOR (Insecure Direct Object Reference) Prevention
To ensure **one user cannot access another user's protected data simply by changing an ID in the request**:
- In `EventInquiryServiceImpl`, every resource access operation (`getInquiryById`, `updateInquiry`, `deleteInquiry`) executes the `verifyOwnershipOrAdmin` security check:
```java
private void verifyOwnershipOrAdmin(EventInquiry inquiry, User currentUser) {
    boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
    boolean isOwner = Objects.equals(inquiry.getUser().getId(), currentUser.getId());

    if (!isAdmin && !isOwner) {
        throw new UnauthorizedAccessException("Access denied: You do not have permission to access or modify this inquiry.");
    }
}
```
If a logged-in standard user attempts to pass another user's inquiry ID (e.g. `GET /api/v1/inquiries/42`), the application returns an immediate **HTTP 403 Forbidden**.

---

## 🚀 API Endpoints Reference

### Public / Authentication Endpoints
| HTTP Method | Endpoint Path | Authorization | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register a new user account | `201 Created` |
| `POST` | `/api/v1/auth/login` | Public | Authenticate & receive JWT token | `200 OK` |
| `GET` | `/api/v1/auth/me` | Authenticated | Get current logged-in user profile | `200 OK` |

### Event Inquiry Endpoints
| HTTP Method | Endpoint Path | Authorization | Description | Status Code |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/inquiries` | `ROLE_USER` / `ADMIN` | Submit a new event inquiry | `201 Created` |
| `GET` | `/api/v1/inquiries/my` | `ROLE_USER` / `ADMIN` | List inquiries owned by logged-in user (Paginated) | `200 OK` |
| `GET` | `/api/v1/inquiries/{id}` | Owner or `ADMIN` | View specific inquiry details (IDOR Protected) | `200 OK` |
| `PUT` | `/api/v1/inquiries/{id}` | Owner or `ADMIN` | Update inquiry details (IDOR Protected) | `200 OK` |
| `DELETE` | `/api/v1/inquiries/{id}` | Owner or `ADMIN` | Delete inquiry (IDOR Protected) | `204 No Content` |
| `GET` | `/api/v1/inquiries` | `ROLE_ADMIN` Only | View all system inquiries (Filter by status/type) | `200 OK` |
| `PATCH` | `/api/v1/inquiries/{id}/status` | `ROLE_ADMIN` Only | Update inquiry processing status | `200 OK` |

---

## ⚡ Prerequisites & Getting Started

### Option 1: Quick Run with Embedded H2 Database (Zero External Dependencies)
1. Ensure **JDK 17+** is installed on your system.
2. Clone the repository and navigate into the root folder:
   ```bash
   git clone https://github.com/your-username/event-inquiry-api.git
   cd event-inquiry-api
   ```
3. Run the Spring Boot application using Maven:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
4. The server will start on `http://localhost:8080`.
   - H2 Web Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:eventinquirydb`, Username: `sa`, Password: empty)

---

## 🐳 Running with Docker Compose (PostgreSQL Persistence)

To run the application with a real **PostgreSQL 16** database container:

1. Ensure **Docker** and **Docker Compose** are installed and running.
2. Build and start containers:
   ```bash
   docker-compose up --build
   ```
3. The API will wait for PostgreSQL to pass healthchecks before spinning up at `http://localhost:8080`.
4. To bring down the services and volumes:
   ```bash
   docker-compose down -v
   ```

---

## 🧪 Running Tests

Execute the full suite of Unit, MockMvc Integration, and IDOR Security Tests:

```bash
./mvnw clean test
```

### Included Test Highlights:
- **`SecurityAuthorizationTest`**: Verifies that standard users attempting IDOR requests receive `HTTP 403 Forbidden` and that admin operations are locked down.
- **`AuthControllerTest`**: Validates registration and JWT login flows.
- **`EventInquiryServiceTest`**: Unit testing business logic and status validation.

---

## 📖 API Documentation & Postman Collection

### 1. Interactive Swagger UI / OpenAPI Specs
Once the application is running, open your browser:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON Spec**: `http://localhost:8080/v3/api-docs`

To execute authorized requests in Swagger UI:
1. Call `POST /api/v1/auth/login` to obtain an `accessToken`.
2. Click **Authorize** (top right) in Swagger UI, enter `Bearer <your_token>`, and click **Authorize**.

### 2. Postman Collection
A ready-to-import Postman Collection file is located in the repository root:
- **Filename**: [`Event_Inquiry_Management_API.postman_collection.json`](file:///C:/Users/indrajit/Desktop/event-inquiry-api/Event_Inquiry_Management_API.postman_collection.json)

**Feature**: Postman test scripts automatically capture the JWT token upon executing login and update collection variables (`{{jwt_token}}` & `{{admin_jwt_token}}`), enabling seamless request execution!

---

## 🔑 Pre-configured Seed Credentials

The application automatically seeds initial data on startup:

| Account Type | Email | Password | Role | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Admin User** | `admin@example.com` | `admin123` | `ROLE_ADMIN` | Full access to view/manage all inquiries & update statuses |
| **User 1** | `john@example.com` | `user123` | `ROLE_USER` | Standard user with pre-created sample inquiries |
| **User 2** | `jane@example.com` | `user23` | `ROLE_USER` | Standard user for IDOR testing |

---

## 📄 License
This project is released under the Apache 2.0 License.
