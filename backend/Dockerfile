# ═══════════════════════════════════════════════════════════════════
# Sport SaaS - Backend Dockerfile (Multi-stage build)
# ═══════════════════════════════════════════════════════════════════

# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom files first for better caching
COPY pom.xml .
COPY common/pom.xml common/
COPY config/pom.xml config/
COPY tenant/pom.xml tenant/
COPY auth/pom.xml auth/
COPY catalog/pom.xml catalog/
COPY inventory/pom.xml inventory/
COPY order/pom.xml order/
COPY billing/pom.xml billing/
COPY notification/pom.xml notification/
COPY admin/pom.xml admin/
COPY dashboard/pom.xml dashboard/
COPY app/pom.xml app/

# Download dependencies (cached if pom files don't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY . .

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine AS runtime

# Add non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
