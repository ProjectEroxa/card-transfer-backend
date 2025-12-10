# 1. Этап сборки
FROM maven:3.9-eclipse-temurin-22 AS build
WORKDIR /app

# Копируем исходники
COPY pom.xml .
COPY src ./src

# Собираем JAR
RUN mvn clean package -DskipTests

# 2. Этап запуска
FROM eclipse-temurin:22-jre
WORKDIR /app

# Копируем собранный JAR
COPY --from=build /app/target/card-transfer-backend-0.0.1-SNAPSHOT.jar app.jar

# Открываем порт
EXPOSE 5500

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]