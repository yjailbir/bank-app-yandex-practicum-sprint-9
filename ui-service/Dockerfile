FROM bellsoft/liberica-openjdk-alpine:21

COPY ui-service/target/*.jar /ui-service.jar

ENTRYPOINT ["java", "-jar", "/ui-service.jar"]