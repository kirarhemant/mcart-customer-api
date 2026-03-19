FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR=target/customer-api-0.0.1-SNAPSHOT.jar
COPY ${JAR} app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]