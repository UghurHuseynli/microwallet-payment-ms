# ── Stage 1: Build payment-service ────────────────────────────
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copy local Maven repository with custom-linkedlist
COPY .m2 /root/.m2

COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# ── Stage 2: Run ──────────────────────────────────────────────
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
