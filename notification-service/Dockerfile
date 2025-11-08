FROM bellsoft/liberica-openjdk-alpine:21

COPY target/*.jar /notification-service.jar

ENTRYPOINT ["java", "-jar", "/notification-service.jar"]