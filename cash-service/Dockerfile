FROM bellsoft/liberica-openjdk-alpine:21

COPY target/*.jar /cash-service.jar

ENTRYPOINT [ "java", "-jar", "/cash-service.jar"]