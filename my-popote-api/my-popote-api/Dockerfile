# Étape 1 : compiler l'API avec Maven et Java 17.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copier d'abord le POM pour profiter du cache des dépendances Maven.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copier les sources et produire le JAR exécutable.
COPY src ./src
RUN mvn -B package -DskipTests

# Étape 2 : exécuter l'API sans embarquer Maven ni les sources.
FROM eclipse-temurin:17-jre

WORKDIR /app

# Exécuter l'application avec un utilisateur non privilégié.
RUN useradd --system --uid 10001 --create-home appuser

COPY --from=build /app/target/my-popote-api-0.0.1-SNAPSHOT.jar /app/app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]