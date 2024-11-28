# Use an official OpenJDK runtime as a parent image
FROM openjdk:17.0

# Set the working directory inside the container
WORKDIR /jcraft-userman

# Define a build argument for the version
ARG VERSION
RUN echo "VERSION is ${VERSION}"
# Copy the Spring Boot application JAR file into the container
# Ensure the jar file is correctly named during the build process (e.g., jcraft-userman.jar)
COPY build/libs/userman-${VERSION}.jar userman.jar

# Expose the port your application runs on (default is 8080 for Spring Boot)
EXPOSE 8080

# Set environment variables (optional)
# Uncomment and set if needed, e.g., active Spring profiles
# ENV SPRING_PROFILES_ACTIVE=prod

# Command to run the application
CMD ["java", "-jar", "userman.jar"]
