FROM bellsoft/liberica-openjdk-alpine:21

COPY target/*.jar /ui-service.jar

ENTRYPOINT ["java", "-jar", "/ui-service.jar"]