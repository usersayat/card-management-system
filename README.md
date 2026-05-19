# card-management-system

## О проекте

`card-management-system` — Java Spring Boot приложение для управления банковскими картами и пользователями. Система реализует:

- регистрацию и авторизацию через JWT
- роли `ROLE_USER` и `ROLE_ADMIN`
- выпуск, просмотр, пополнение, перевод и блокировку карт
- управление пользователями и правами доступа
- автогенерируемую документацию OpenAPI / Swagger

## Технологии

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security OAuth2 Resource Server
- Spring Validation
- SpringDoc OpenAPI UI
- MySQL JDBC Driver
- Maven Wrapper (`mvnw`, `mvnw.cmd`)

## Структура проекта

- `src/main/java/com/card_management_system/controller` — REST API контроллеры
- `src/main/java/com/card_management_system/service` — сервисы
- `src/main/java/com/card_management_system/repository` — репозитории данных
- `src/main/java/com/card_management_system/entity` — JPA сущности
- `src/main/java/com/card_management_system/dto` — DTO для запросов и ответов
- `src/main/java/com/card_management_system/config` — конфигурация безопасности и OpenAPI

## Требования

- Java 17
- Maven 3.x или Maven Wrapper
- MySQL сервер

## Конфигурация

Приложение читает параметры подключения к базе данных из переменных окружения:

- `DB_URL` — JDBC URL MySQL
- `DB_USERNAME` — имя пользователя базы данных
- `DB_PASSWORD` — пароль базы данных

Пример для PowerShell / Windows CMD:

```powershell
set DB_URL=jdbc:mysql://localhost:3306/card_db
set DB_USERNAME=root
set DB_PASSWORD=secret
```

Пример для Linux/macOS:

```bash
export DB_URL=jdbc:mysql://localhost:3306/card_db
export DB_USERNAME=root
export DB_PASSWORD=secret
```

В `src/main/resources/application.yaml` также заданы значения по умолчанию:

- `jwt.secret` — секретный ключ для подписи JWT
- `admin.registration-key` — ключ для регистрации администратора
- `server.port` — порт приложения (`8080` по умолчанию)

> Для безопасного развёртывания замените значения `jwt.secret` и `admin.registration-key` на собственные.

## Запуск

Из корневой папки проекта выполните один из вариантов.

### Запуск через Maven Wrapper

Windows:

```powershell
.\\mvnw.cmd clean package
.\\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw clean package
./mvnw spring-boot:run
```

### Запуск через Maven

```bash
mvn clean package
mvn spring-boot:run
```

После запуска приложение доступно по адресу:

```text
http://localhost:8080
```

## API документация

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Endpoints

### Аутентификация

- `POST /api/auth/register` — регистрация пользователя
- `POST /api/auth/login` — вход и получение JWT

### Работа с картами

- `POST /api/cards/issue` — выпустить карту (`ROLE_ADMIN`)
- `GET /api/cards/{id}` — получить карту по ID (`ROLE_ADMIN`)
- `GET /api/cards/search` — поиск карт текущего пользователя
- `GET /api/cards/{id}/cards` — получить карты текущего пользователя
- `GET /api/cards/user-cards` — получить карты указанного пользователя (`ROLE_ADMIN`)
- `GET /api/cards/{id}/balance` — получить баланс карты
- `DELETE /api/cards/{id}` — удалить карту (`ROLE_ADMIN`)
- `PATCH /api/cards/{id}/deposit` — пополнить баланс карты
- `PATCH /api/cards/{idOfCardFrom}/transfer` — перевести средства между картами
- `PATCH /api/cards/{id}/block` — заблокировать карту для пользователя
- `PATCH /api/cards/{id}/activate-for-admin` — активировать карту (`ROLE_ADMIN`)
- `PATCH /api/cards/{id}/block-for-admin` — заблокировать карту (`ROLE_ADMIN`)

### Пользователи

- `GET /api/users/{id}` — получить пользователя по ID (`ROLE_ADMIN`)
- `GET /api/users` — получить всех пользователей (`ROLE_ADMIN`)
- `PUT /api/users/{id}` — обновить пользователя (`ROLE_ADMIN`)
- `DELETE /api/users/{id}` — удалить пользователя (`ROLE_ADMIN`)
- `PATCH /api/users/{id}/role` — изменить роль пользователя (`ROLE_ADMIN`)

## Примечания

- Для большинства защищённых эндпоинтов необходимо передавать заголовок `Authorization: Bearer <JWT>`.
- Схема базы данных управляется через JPA `hibernate.ddl-auto=update`.
- Настройки базы данных задаются через переменные окружения.
