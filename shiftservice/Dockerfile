# -------- BUILD STAGE --------
FROM docker.io/maven:3.9.11-eclipse-temurin-25 AS builder
WORKDIR /app

COPY pom.xml .
COPY shiftcontrol-lib/pom.xml shiftcontrol-lib/
COPY shiftservice/pom.xml shiftservice/

RUN mvn -pl shiftservice -am dependency:go-offline

# 3️⃣ Copy actual source
COPY shiftcontrol-lib shiftcontrol-lib
COPY shiftservice shiftservice

RUN mvn -pl shiftservice -am clean package -DskipTests

# -------- RUNTIME STAGE --------
FROM docker.io/eclipse-temurin:25-jre-alpine AS server
WORKDIR /app
COPY --from=builder /app/shiftservice/target/shiftservice-*.jar /app/app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
