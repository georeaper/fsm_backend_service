FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN ./gradlew shadowJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*all.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]