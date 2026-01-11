# ---------- builder ----------
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

# Better build caching: copy only Gradle metadata first
COPY gradlew settings.gradle* build.gradle* gradle.properties* /build/
COPY gradle /build/gradle
RUN ./gradlew --no-daemon -v

# Now copy sources
COPY . /build

# Build
RUN ./gradlew clean bootJar --no-daemon

# ---------- runtime ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Create non-root user (Debian/Ubuntu style)
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the jar and set ownership
COPY --from=builder --chown=appuser:appuser /build/build/libs/*.jar /app/app.jar

USER appuser
EXPOSE 8080

# Prefer JAVA_TOOL_OPTIONS so you can use exec-form ENTRYPOINT (no shell)
ENV JAVA_TOOL_OPTIONS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]