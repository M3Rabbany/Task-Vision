FROM openjdk:17-jdk-alpine
LABEL authors="Enigma_Red_Teams"

WORKDIR /app

COPY target/task-vision-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
