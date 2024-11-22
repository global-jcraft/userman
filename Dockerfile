# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory
WORKDIR /jcraft-userman

# Copy the JAR file into the container
COPY build/libs/your-app.jar jcraft-userman.jar

# Expose the port your app runs on (optional, replace 8080 with your app's port)
EXPOSE 8080

# Command to run the JAR file
CMD ["java", "-jar", "jcraft-userman.jar"]