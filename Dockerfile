# build stage
FROM maven:3.9.4-eclipse-temurin-17 as build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw
COPY src src
RUN ./mvnw -B -DskipTests package

# runtime stage
FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && apt-get install -y \
    fonts-dejavu-core \
    fonts-liberation \
    fonts-noto-core \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]