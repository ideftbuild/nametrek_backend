# Use a Java 21 base image
FROM eclipse-temurin:21-jdk-jammy

# Set the working directory inside the container
WORKDIR /app

# Cpoy the built JAR file into the container
COPY build/libs/*.jar app.jar

# Define the default command to run your app
ENTRYPOINT ["java","-jar","app.jar"]
