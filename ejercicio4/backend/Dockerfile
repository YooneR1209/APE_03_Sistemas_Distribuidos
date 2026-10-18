# Etapa 1: Compilación
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY . .
# Permiso de ejecución al wrapper de Maven y compilación del JAR
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Etapa 2: Ejecución ligera
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]