# ── Stage 1: Build custom-linkedlist ──────────────────────────
FROM eclipse-temurin:25-jdk AS lib-builder
WORKDIR /lib

COPY custom-linkedlist/ .
RUN chmod +x gradlew && ./gradlew publishToMavenLocal --no-daemon

# ── Stage 2: Build payment-service ────────────────────────────
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# copy the local maven repo from previous stage
COPY --from=lib-builder /root/.m2 /root/.m2

COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# ── Stage 3: Run ──────────────────────────────────────────────
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]