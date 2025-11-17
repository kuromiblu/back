FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY target/safe-line-0.0.1-SNAPSHOT.jar safeline-api.jar


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "safeline-api.jar"]

