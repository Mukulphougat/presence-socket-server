# Start from a Java image
FROM openjdk:17-jdk-slim
LABEL authors="mukul"

# Set the working directory
WORKDIR /app

# Copy the jar file
#COPY ./target/user-presence-socket-server-0.0.1-SNAPSHOT.jar app.jar
COPY target/presence-socket-server-0.0.1-SNAPSHOT.jar app.jar

# expose port
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
