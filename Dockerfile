FROM openjdk:17

EXPOSE 8080

# The application's jar file
ARG JAR_FILE=build/libs/*.jar

# Add the application's jar to the container
COPY ${JAR_FILE} MARU_EGG_BE.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "MARU_EGG_BE.jar"]