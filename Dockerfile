# Multi-stage Docker build for Event Inquiry Management API
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy dependency descriptor and wrapper
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Copy source code and package application
COPY src src
RUN ./mvnw package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy jar artifact from build stage
COPY --from=build /app/target/event-inquiry-api-1.0.0.jar app.jar

ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar"]
