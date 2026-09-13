# ==============================================================================
# Stage 1: Build React Frontend
# ==============================================================================
FROM node:20-alpine AS frontend-builder
WORKDIR /frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

# ==============================================================================
# Stage 2: Build Spring Boot Backend with Bundled Frontend
# ==============================================================================
FROM maven:3.9-eclipse-temurin-17 AS backend-builder
WORKDIR /build

# Pre-fetch project dependencies using pom.xml
COPY pom.xml ./
RUN mvn dependency:go-offline -B || true

# Copy backend source code
COPY src src

# Copy compiled React frontend assets directly into Spring Boot static resources
COPY --from=frontend-builder /frontend/dist src/main/resources/static/

# Build unified fat JAR
RUN mvn clean package -DskipTests

# ==============================================================================
# Stage 3: Production Runtime Image (Lightweight JRE)
# ==============================================================================
FROM eclipse-temurin:17-jre-jammy AS runner
WORKDIR /app

# Dedicated non-root user for enterprise container security
RUN groupadd -r bankflow && useradd -r -g bankflow -m -d /app bankflow

COPY --from=backend-builder /build/target/bankflow-backend-*.jar /app/bankflow-backend.jar
RUN chown -R bankflow:bankflow /app

USER bankflow:bankflow

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app/bankflow-backend.jar"]
