FROM bellsoft/liberica-openjdk-alpine:21

COPY exchange-generator-service/target/*.jar /exchange-generator-service.jar

ENTRYPOINT ["java", "-jar", "/exchange-generator-service.jar"]