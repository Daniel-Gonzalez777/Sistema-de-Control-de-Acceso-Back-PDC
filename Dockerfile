# Etapa 1: compilar el proyecto con Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: imagen final, solo con el JAR ya compilado (más liviana)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/acceso-api-0.1.0.jar app.jar

# Render inyecta la variable PORT automáticamente; application-prod.properties
# ya está preparado para leerla (server.port=${PORT:8080})
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
