# ==============================================================================
# Stage 1: Build the Application
# ==============================================================================
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build

# Copy Maven wrapper and pom.xml first for efficient layer caching
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Pre-fetch project dependencies
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production jar
COPY src src
RUN ./mvnw clean package -DskipTests

# ==============================================================================
# Stage 2: Production Runtime Image
# ==============================================================================
FROM eclipse-temurin:17-jre-jammy AS runner

WORKDIR /app

# Create a dedicated non-root user for security
RUN groupadd -r bankflow && useradd -r -g bankflow -m -d /app bankflow

# Copy compiled fat JAR from builder stage
COPY --from=builder /build/target/bankflow-backend-*.jar /app/bankflow-backend.jar
RUN chown -R bankflow:bankflow /app

USER bankflow:bankflow

EXPOSE 8080

# Production JVM optimizations: container-aware memory allocation
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app/bankflow-backend.jar"]
