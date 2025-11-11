# Stage 1: Build the native image
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copy project files
COPY pom.xml .
COPY src ./src

# Build native executable
RUN mvn -Pnative -DskipTests clean package

# Stage 2: Run stage
FROM ubuntu:22.04

# Add a user for security (optional)
RUN useradd -m appuser

WORKDIR /app

# Create writable logs directory
RUN mkdir -p /app/logs && chmod 777 /app/logs

# Copy native binary
COPY --from=builder /workspace/target/* /app/

ENV CASHDESK_LOG_DIR=/app/logs

USER appuser

EXPOSE 8080

ENTRYPOINT ["./cashoperations"]
