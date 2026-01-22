# -------- BUILD STAGE --------
FROM docker.io/maven:3.9.11-eclipse-temurin-25 AS builder
WORKDIR /app

# 1️⃣ Copy POMs first (for dependency caching)
COPY pom.xml .
COPY shiftcontrol-lib/pom.xml shiftcontrol-lib/
COPY shiftservice/pom.xml shiftservice/
COPY pretalx-client/pom.xml pretalx-client/
COPY trustservice/pom.xml trustservice/
COPY auditlog/pom.xml auditlog/

# 2️⃣ Pre-fetch dependencies for auditlog
RUN mvn -pl auditlog -am dependency:go-offline

# 3️⃣ Copy actual source code
COPY shiftcontrol-lib shiftcontrol-lib
COPY shiftservice shiftservice
COPY pretalx-client pretalx-client
COPY trustservice trustservice
COPY auditlog auditlog

# 4️⃣ Build auditlog (and required modules)
RUN mvn -pl auditlog -am clean package -DskipTests


# -------- RUNTIME STAGE --------
FROM docker.io/eclipse-temurin:25-jre-alpine AS server
WORKDIR /app

# 5️⃣ Copy auditlog JAR only
COPY --from=builder /app/auditlog/target/auditlog-*.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
