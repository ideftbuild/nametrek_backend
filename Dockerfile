# Stage 1: Build the jar
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# Copy source code
COPY src src

# Make gradlew executable
RUN chmod +x gradlew

# Build the jar inside container and skip tests
RUN ./gradlew build -x test --no-daemon

# Stage 2: Run the app
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy only the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
