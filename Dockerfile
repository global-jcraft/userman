FROM openjdk:21-slim AS builder
WORKDIR /build
COPY . .
RUN ./gradlew clean bootJar --no-daemon

FROM openjdk:21-slim
WORKDIR /app

RUN addgroup --system appuser && adduser --system --group appuser

COPY --from=builder /build/build/libs/*.jar app.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
