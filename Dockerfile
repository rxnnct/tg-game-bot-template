FROM openjdk:21-jdk-slim

WORKDIR /build

COPY game-app/target/game-app-1.0-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]