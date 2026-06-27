# Use a lightweight JDK runtime
FROM eclipse-temurin:20-jre-alpine

WORKDIR /app

# Copy the shadow/fat JAR built locally
COPY ktor-sample-all.jar app.jar

# Copy your env files if needed
COPY assets ./assets

# Expose the port your Ktor app uses
EXPOSE 8080

# Optional: set environment variable
ENV ENV=prod

# Run the app
CMD ["java", "-jar", "app.jar"]