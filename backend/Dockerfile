ARG DOCKER_REGISTRY=docker.m.daocloud.io/library
FROM ${DOCKER_REGISTRY}/maven:3.9-eclipse-temurin-17 AS build
ARG MAVEN_MIRROR_URL

WORKDIR /app

COPY maven-settings.xml /tmp/maven-settings.xml
COPY pom.xml .
RUN mvn -s /tmp/maven-settings.xml dependency:go-offline -B -ntp

COPY src ./src
RUN mvn -s /tmp/maven-settings.xml clean package -DskipTests -B -ntp

FROM ${DOCKER_REGISTRY}/eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
