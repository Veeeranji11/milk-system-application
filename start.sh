#!/usr/bin/env bash
set -e

echo "==================================================================="
echo "  Starting Village Milk Collection & Money Management System"
echo "==================================================================="

# Build JAR
echo "[1/2] Building and Packaging Application..."
./mvnw clean package -DskipTests=true

# Run Application
echo "[2/2] Launching Spring Boot Server on http://localhost:8080 ..."
java -jar target/MilkSystemApplication-0.0.1-SNAPSHOT.jar
