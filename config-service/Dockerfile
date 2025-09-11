FROM bellsoft/liberica-openjdk-alpine:21

COPY config-service/target/*.jar /config-service.jar

ENTRYPOINT ["java","-jar","/config-service.jar"]