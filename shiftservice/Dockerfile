FROM docker.io/maven:3.9.11-eclipse-temurin-25 as builder

WORKDIR /app

COPY pom.xml .
COPY shiftcontrol-lib shiftcontrol-lib
COPY shiftservice shiftservice
RUN mvn -pl shiftservice -am clean package -DskipTests

FROM docker.io/eclipse-temurin:25-jre-alpine as server
COPY --from=builder /app/shiftservice/target/shiftservice-*.jar /app/app.jar

WORKDIR /app
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
