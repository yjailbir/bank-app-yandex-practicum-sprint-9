FROM bellsoft/liberica-openjdk-alpine:21

COPY target/*.jar /blocker-service.jar

ENTRYPOINT ["java", "-jar", "/blocker-service.jar"]