# Requiere haber creado el jar: mvn clean package
FROM eclipse-temurin:23-jre-alpine
# Copiar el .jar
COPY target/*.jar app.jar
# ejecutar el archivo jar con java, añadir VM OPTION si se quiere
CMD ["java", "-jar", "app.jar"]

# Construir imagen docker
# docker build -t spring:1.0.0 .

# Ejecutar imagen docker:
# Opción 1: con docker run
# docker run -p 8080:8080 spring:1.0.0

# Opción 2: con docker compose
# Opción 3: Kubernetes / Helm