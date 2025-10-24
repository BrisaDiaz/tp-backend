# Usar una imagen base de Java (JRE ligero)
FROM eclipse-temurin:21-jre-alpine

# Argumento para el nombre del archivo JAR (definido en el Maven build)
ARG JAR_FILE=target/*.jar

# Copia el JAR generado al contenedor
COPY ${JAR_FILE} app.jar

# Define el punto de entrada para ejecutar la aplicación
ENTRYPOINT ["java","-jar","/app.jar"]

# Opcional: Define un usuario menos privilegiado para seguridad
# USER 1000