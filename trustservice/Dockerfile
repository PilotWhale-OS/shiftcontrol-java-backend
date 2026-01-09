# -------- BUILD STAGE --------
FROM docker.io/maven:3.9.11-eclipse-temurin-25 AS builder
WORKDIR /app

# 1️⃣ Copy POMs first (for dependency caching)
COPY pom.xml .
COPY shiftcontrol-lib/pom.xml shiftcontrol-lib/
COPY shiftservice/pom.xml shiftservice/
COPY pretalx-client/pom.xml pretalx-client/
COPY trustservice/pom.xml trustservice/

# 2️⃣ Pre-fetch dependencies for trustservice
RUN mvn -pl trustservice -am dependency:go-offline

# 3️⃣ Copy actual source code
COPY shiftcontrol-lib shiftcontrol-lib
COPY shiftservice shiftservice
COPY pretalx-client pretalx-client
COPY trustservice trustservice

# 4️⃣ Build trustservice (and required modules)
RUN mvn -pl trustservice -am clean package -DskipTests


# -------- RUNTIME STAGE --------
FROM docker.io/eclipse-temurin:25-jre-alpine AS server
WORKDIR /app

# 5️⃣ Copy trustservice JAR only
COPY --from=builder /app/trustservice/target/trustservice-*.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
