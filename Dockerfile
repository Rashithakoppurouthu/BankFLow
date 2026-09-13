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
FROM eclipse-temurin:17-jdk-jammy AS backend-builder
WORKDIR /build

# Copy Maven wrapper and pom.xml first for layer caching
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Fix Windows CRLF line-endings and ensure execution permission on Linux
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

RUN ./mvnw dependency:go-offline -B || true

# Copy source code
COPY src src

# Copy compiled React frontend assets directly into Spring Boot static resources
COPY --from=frontend-builder /frontend/dist src/main/resources/static/

# Build unified fat JAR
RUN ./mvnw clean package -DskipTests

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
