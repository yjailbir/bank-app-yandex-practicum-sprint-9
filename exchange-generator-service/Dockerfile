FROM bellsoft/liberica-openjdk-alpine:21

COPY target/*.jar /exchange-generator-service.jar

ENTRYPOINT ["java", "-jar", "/exchange-generator-service.jar"]