# ==============================================================================
# Multi-Stage Dockerfile: React Frontend + Spring Boot Backend
# Produces a single self-contained JAR that serves the React SPA
# ==============================================================================

# ── Stage 1: Build React Frontend ──────────────────────────────────────────────
FROM node:20-alpine AS frontend-builder
WORKDIR /frontend

# Install dependencies first (layer cache)
COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install --frozen-lockfile 2>/dev/null || npm install

# Copy all frontend source and build
COPY frontend/ ./
RUN npm run build

# ── Stage 2: Build Spring Boot Application ─────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS backend-builder
WORKDIR /app

# Download dependencies first (layer cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copy source
COPY src ./src

# Copy the built React SPA into Spring Boot's static resources
COPY --from=frontend-builder /frontend/dist ./src/main/resources/static/app

# Package the JAR (tests skipped for Docker build)
RUN mvn clean package -DskipTests -q

# ── Stage 3: Minimal Runtime Image ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create cloud-storage directory (for PDF/invoice storage)
RUN mkdir -p /app/cloud-storage && chown -R appuser:appgroup /app

USER appuser

# Copy the assembled JAR
COPY --from=backend-builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
