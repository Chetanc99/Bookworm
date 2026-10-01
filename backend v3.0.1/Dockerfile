# Multi-stage build: compile with a full JDK+Maven image, run on a slim JRE.
# Keeps the shipped image small and doesn't require Maven/JDK on the host
# doing the deployment - only Docker itself.

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Copy the pom first so dependency resolution is cached across builds unless
# pom.xml itself changes - avoids re-downloading the internet on every rebuild.
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Uploaded files (covers/content, see FileStorageServiceImpl) live under
# ./uploads by default - mount a volume here in any real deployment or
# they're lost when the container is recreated.
VOLUME ["/app/uploads"]

EXPOSE 8080

# SPRING_PROFILES_ACTIVE defaults to prod in the container (unlike local
# dev, where application.yml's own default of "dev" applies) - a container
# image is, by definition, being run somewhere other than a dev laptop.
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
