FROM bellsoft/liberica-openjdk-alpine:21

COPY target/*.jar /exchange-service.jar

ENTRYPOINT ["java", "-jar", "/exchange-service.jar"]