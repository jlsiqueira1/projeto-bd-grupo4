# ---------- Etapa 1: compila o projeto com Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa as dependências antes de copiar o código: assim o Docker reaproveita
# essa camada em builds seguintes enquanto o pom.xml não mudar.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# ---------- Etapa 2: imagem final, só com o JRE e o .jar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/prf-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
