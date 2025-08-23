FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jre-alpine as runtime
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 2003
ENTRYPOINT ["java", "-jar", "app.jar"]