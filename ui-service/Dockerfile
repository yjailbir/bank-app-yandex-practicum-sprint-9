FROM bellsoft/liberica-openjdk-alpine:21

RUN apk add --no-cache bash netcat-openbsd

COPY ui-service/target/*.jar /ui-service.jar

COPY wait-for-consul.sh /wait-for-consul.sh

RUN chmod +x /wait-for-consul.sh

ENTRYPOINT ["/wait-for-consul.sh", "consul", "8500", "java","-jar","/ui-service.jar"]