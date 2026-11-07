# Etapa de compilación: JDK 21 + Maven (la imagen base ya incluye ambos,
# por eso no se usa el wrapper, cuyo jar ademas no se distribuye en Git).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar el pom primero para que las descargas de dependencias queden
# cacheadas en una capa propia (los builds con cambios de codigo son rapidos)
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

# Etapa de ejecucion: solo JRE, sin fuentes ni Maven (imagen final pequena)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/pruebaAccenture-0.0.1-SNAPSHOT.jar app.jar

# La aplicacion no debe correr como root dentro del contenedor
USER 1000

ENTRYPOINT ["java", "-jar", "app.jar"]
