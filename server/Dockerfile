# ---------- Build ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml ./

RUN mvn dependency:go-offline -B

COPY src/ src/

RUN mvn clean package -DskipTests -B

# ---------- Runtime ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --create-home spring

COPY --from=build /app/target/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]