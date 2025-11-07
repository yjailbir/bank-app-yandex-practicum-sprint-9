FROM bellsoft/liberica-openjdk-alpine:21

COPY target/*.jar /accounts-service.jar

ENTRYPOINT ["java", "-jar", "accounts-service.jar"]