FROM openjdk:17

EXPOSE 8080

# The application's jar file
ARG JAR_FILE=build/libs/MARU_EGG-0.0.1-SNAPSHOT.jar

# Copy the application's jar to the container
COPY ${JAR_FILE} MARU_EGG.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "MARU_EGG.jar"]