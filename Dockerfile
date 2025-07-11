# Use an official OpenJDK 11 base image
FROM openjdk:11-jdk-slim
RUN mkdir -p /app/logs

# Set the working directory inside the container
WORKDIR /app

# Add metadata (optional)
LABEL maintainer="your-email@example.com"

# Copy the JAR file built by Maven/Gradle
COPY event-receiver-1.0.jar app.jar

# Expose port 8080 (or whichever port your Spring Boot app uses)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
