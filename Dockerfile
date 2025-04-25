# Build stage
FROM openjdk:21-slim AS builder
WORKDIR /build
COPY . .
# This command builds the bootJar for the 'bootstrap' module
RUN ./gradlew clean bootJar --no-daemon

# Runtime stage
FROM openjdk:21-slim
WORKDIR /app

# Create a non-root user
RUN addgroup --system appuser && adduser --system --group appuser

# Copy only the built jar from the build stage's bootstrap module output
# Adjust the JAR filename pattern if your versioning or naming differs
COPY --from=builder /build/build/libs/*.jar app.jar

USER appuser

# Define the entry point
ENTRYPOINT ["java", "-jar", "app.jar"]
