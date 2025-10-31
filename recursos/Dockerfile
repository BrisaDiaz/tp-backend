# ---- Etapa 1: Build ----
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiamos solo los POMs primero (para cachear dependencias)
COPY pom.xml ./

# Descargamos dependencias (se cachea mientras no cambie el pom.xml)
RUN mvn dependency:go-offline -B

# Copiamos el código fuente del servicio
COPY src ./src

# Compilamos el proyecto (sin tests)
RUN mvn clean package -DskipTests

# ---- Etapa 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el JAR generado desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
