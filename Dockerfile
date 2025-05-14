FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# Copy source code
COPY src src

# Make gradlew executable
RUN chmod +x gradlew

# Build the jar inside container
RUN ./gradlew build --no-daemon

# Copy the built jar to app.jar
RUN cp build/libs/*.jar app.jar

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
