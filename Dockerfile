# =========================================================================
# SentinelTrace - Multi-Stage Production Dockerfile (Java 21 + Spring Boot)
# =========================================================================

# Stage 1: Build the Application JAR
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy Maven descriptor and wrapper
COPY pom.xml mvnw ./
COPY .mvn .mvn/
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy application source code and compile package
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Lightweight Runtime Image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create unprivileged application user for security best practices
RUN addgroup -S sentinel && adduser -S sentinel -G sentinel
USER sentinel:sentinel

# Copy compiled JAR artifact from builder stage
COPY --from=builder --chown=sentinel:sentinel /workspace/target/sentineltrace-1.0.0.jar app.jar

# Expose default HTTP server port
EXPOSE 8081
ENV SERVER_PORT=8081

# JVM flags optimized for containerized environments
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
