# ------------------------------
# Stage 1: Build the application
# ------------------------------
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

# Cache dependencies first
COPY pom.xml ./
RUN mvn -B -q -e -U dependency:go-offline

# Copy sources and build
COPY src ./src
RUN mvn -B -q -DskipTests package

# ------------------------------
# Stage 2: Run the application
# ------------------------------
FROM eclipse-temurin:17-jre

# Create non-root user
RUN useradd -ms /bin/bash appuser
USER appuser

WORKDIR /app

# Copy the built jar from the builder stage
# The artifact name is defined in pom.xml as cashoperations-0.0.1-SNAPSHOT.jar
COPY --from=builder /workspace/target/cashoperations-0.0.1-SNAPSHOT.jar /app/app.jar

# Environment variables (can be overridden at runtime)
# Example: docker run -e FIB_AUTH_API_KEY=your-key ...
ENV JAVA_OPTS=""

# Expose Spring Boot default port
EXPOSE 8080

# Run the application
# Use shell form to allow JAVA_OPTS expansion
ENTRYPOINT sh -c "java $JAVA_OPTS -jar /app/app.jar"