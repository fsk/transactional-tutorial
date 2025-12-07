# Spring Boot Application için Dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Root pom.xml ve tüm modülleri kopyala
COPY pom.xml .
COPY transaction-*/pom.xml ./transaction-*/
COPY transaction-*/src ./transaction-*/src/

# Maven build (sadece belirtilen modülü build et)
ARG MODULE_NAME
RUN mvn clean package -pl ${MODULE_NAME} -am -DskipTests

# Runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Build edilmiş JAR'ı kopyala
ARG MODULE_NAME
COPY --from=build /app/${MODULE_NAME}/target/*.jar app.jar

# Port (modüle göre değişir, environment variable ile override edilebilir)
ENV SERVER_PORT=8080

EXPOSE ${SERVER_PORT}

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]



