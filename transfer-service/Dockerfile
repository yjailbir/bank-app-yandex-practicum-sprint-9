FROM bellsoft/liberica-openjdk-alpine:21

COPY target/*.jar /transfer-service.jar

ENTRYPOINT ["java", "-jar", "/transfer-service.jar"]