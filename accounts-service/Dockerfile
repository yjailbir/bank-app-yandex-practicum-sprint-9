FROM bellsoft/liberica-openjdk-alpine:21

RUN apk add --no-cache bash netcat-openbsd

COPY accounts-service/target/*.jar /accounts-service.jar

COPY wait-for-config-server.sh /wait-for-config-server.sh

RUN chmod +x /wait-for-config-server.sh

ENTRYPOINT ["/wait-for-config-server.sh", "config-service", "8080", "java", "-jar", "accounts-service.jar"]