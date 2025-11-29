FROM docker.io/eclipse-temurin:23-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080 8081

CMD ["java", "-jar", "app.jar"]
