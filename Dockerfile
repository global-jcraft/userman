# Build stage
FROM openjdk:21-slim AS builder
WORKDIR /build
COPY . .
RUN ./gradlew clean bootJar

# Runtime stage
FROM openjdk:21-slim
WORKDIR /app

# Create a non-root user
RUN addgroup --system appuser && adduser --system --group appuser
USER appuser

# Copy only the built jar from the build stage
COPY --from=builder /build/build/libs/huddey-core-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -q --spider http://localhost:8080/userman/actuator/health || exit 1

# Set memory limits and enable GC logging
ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-XX:+UseG1GC", "-jar", "app.jar"]
