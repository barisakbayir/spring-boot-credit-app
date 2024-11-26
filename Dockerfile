# Base image
FROM openjdk:17-jdk-slim

COPY target/credit-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "/app.jar"]