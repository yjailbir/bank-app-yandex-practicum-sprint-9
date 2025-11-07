FROM bellsoft/liberica-openjdk-alpine:21

COPY blocker-service/target/*.jar /blocker-service.jar

ENTRYPOINT ["java", "-jar", "/blocker-service.jar"]