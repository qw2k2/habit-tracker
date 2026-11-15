FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -B -ntp clean verify
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/habit-tracker-1.0.0.jar app.jar
ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
