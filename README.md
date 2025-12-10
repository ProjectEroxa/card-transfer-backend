# Card Transfer Backend

Spring Boot REST API для перевода денег между банковскими картами.

## 🚀 Запуск приложения

### Вариант 1: Docker (рекомендуется)

# Сборка и запуск контейнера
docker-compose up --build

# Остановка
docker-compose down

### Вариант 2: Локальный запуск

# Сборка
mvn clean package

# Запуск
java -jar target/card-transfer-backend-0.0.1-SNAPSHOT.jar
📍 Порт
Приложение доступно по адресу: http://localhost:5500

###📞 API Endpoints
1. Перевод денег
POST /transfer

## Пример запроса:

json
{
  "cardFromNumber": "1234567812345678",
  "cardFromValidTill": "12/25",
  "cardFromCVV": "123",
  "cardToNumber": "8765432187654321",
  "amount": {
    "value": 1000,
    "currency": "RUB"
  }
}
## Пример успешного ответа (200 OK):

json
{
  "operationId": "639546"
}
## Пример ошибки (400 Bad Request):

json
{
  "message": "Error input data",
  "operationId": "789012"
}

### 2. Подтверждение операции
POST /confirmOperation

## Пример запроса:

json
{
  "operationId": "639546",
  "code": "1740"
}
## Пример ответа:

json
{
  "operationId": "639546"
}
