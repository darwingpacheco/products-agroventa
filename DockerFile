# ─── 1. Build Stage ───────────────────────────────────────────────────────────
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copiar solo el pom.xml para descargar y cachear las dependencias
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiar el código fuente y compilar la aplicación
COPY src ./src
RUN mvn -B -DskipTests clean package

# ─── 2. Runtime Stage ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el JAR generado usando comodín para evitar depender de la versión fija
COPY --from=build /workspace/target/*.jar app.jar

# Cambiar permisos al usuario no-root
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]