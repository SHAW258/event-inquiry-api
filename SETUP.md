# ⚙️ Event Inquiry Management API - Installation & Setup Guide

This document provides step-by-step instructions for running, testing, and deploying the **Event Inquiry Management API**.

---

## 📌 Prerequisites

Before running the application, ensure your environment meets the following requirements:

- **Java Development Kit (JDK)**: Version **17** or **21** (e.g., OpenJDK / Eclipse Temurin / JetBrains Runtime)
- **Maven**: Version **3.8+** (Optional: bundled `./mvnw` script is provided)
- **Docker & Docker Compose**: (Optional, required for PostgreSQL container execution)
- **Git**: Installed and configured
- **Postman**: (Optional, for API testing)

---

## 🛠️ Environment Configuration & Profiles

The application includes three environment configuration profiles:

1. **`dev` Profile (Embedded H2 Database)**:
   - File: `src/main/resources/application-dev.yml`
   - Database: H2 In-Memory (`jdbc:h2:mem:eventinquirydb`)
   - Purpose: Zero-configuration instant local development and evaluation.

2. **`docker` Profile (PostgreSQL Database Container)**:
   - File: `src/main/resources/application-docker.yml`
   - Database: PostgreSQL (`jdbc:postgresql://postgres:5432/event_inquiry_db`)
   - Purpose: Containerized deployment using `docker-compose up`.

3. **`test` Profile (Automated Test Execution)**:
   - File: `src/test/resources/application-test.yml`
   - Database: H2 In-Memory (`jdbc:h2:mem:testdb`)
   - Purpose: Executing JUnit 5 unit and integration tests.

---

## 🚀 Option 1: Local Setup with Maven & H2 (Recommended for Rapid Testing)

### 1. Clone the Repository
```bash
git clone https://github.com/SHAW258/event-inquiry-api.git
cd event-inquiry-api
```

### 2. Set Java Home (If Required)
Ensure `JAVA_HOME` points to your JDK 17/21 installation:
- **Windows (PowerShell)**:
  ```powershell
  $env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
  ```
- **macOS / Linux**:
  ```bash
  export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
  ```

### 3. Compile and Run Application
```bash
# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Verify Server Health
Open your browser or terminal:
- Health Check: `http://localhost:8080/actuator/health` -> `{"status":"UP"}`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 Web Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:eventinquirydb`
  - Username: `sa`
  - Password: *(leave empty)*

---

## 🐳 Option 2: Run with Docker & PostgreSQL

To run the application backed by a real PostgreSQL 16 container:

### 1. Build and Launch Containers
```bash
docker-compose up --build
```

### 2. Verify Container Status
```bash
docker-compose ps
```

### 3. Bring Down Services & Volumes
```bash
docker-compose down -v
```

---

## 🧪 Running Automated Tests

Run the complete test suite (unit tests, MockMvc integration, and IDOR security authorization tests):

```bash
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

Expected Output:
```
[INFO] Results:
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## ☁️ Option 3: Deploying to Railway

The repository is pre-configured with `railway.json` and multi-stage Docker build support.

### 1. Deploy from GitHub Repo
1. Login to your [Railway Account](https://railway.com/).
2. Click **New Project** -> **Deploy from GitHub repo**.
3. Select `SHAW258/event-inquiry-api`.
4. Railway automatically detects `Dockerfile` and `railway.json` to trigger the build.

### 2. (Optional) Add PostgreSQL Plugin on Railway
1. Click **+ New** -> **Database** -> **Add PostgreSQL**.
2. Railway will inject `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` automatically.

---

## 🛠️ Troubleshooting & Common Issues

### Issue 1: `Port 8080 is already in use`
**Fix**: Terminate the existing process using port 8080 or specify a different port:
```bash
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dserver.port=8081"
```

### Issue 2: `Insecure Direct Object Reference (403 Forbidden)`
**Reason**: Non-admin users cannot access or edit inquiries owned by other accounts.
**Fix**: Ensure you authenticate with the owner user's credentials or use the `admin@example.com` account.

### Issue 3: `Script execution disabled on Windows PowerShell`
**Fix**: Run batch command directly via `.\mvnw.cmd` instead of calling `.ps1` files.
