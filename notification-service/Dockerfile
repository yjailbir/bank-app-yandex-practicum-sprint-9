FROM bellsoft/liberica-openjdk-alpine:21

RUN apk add --no-cache bash netcat-openbsd

COPY notification-service/target/*.jar /notification-service.jar

COPY wait-for.sh /wait-for.sh

RUN chmod +x /wait-for.sh

ENTRYPOINT ["/wait-for.sh", "config-service", "8080", "java","-jar","/notification-service.jar"]