# Task Catalog

REST-сервис для управления задачами на Kotlin + Spring Boot + Reactor + JdbcClient.

## Что реализовано

- CRUD для задач
- список задач с пагинацией и опциональной фильтрацией по статусу
- native SQL через `JdbcClient`
- реактивный сервисный слой на `Mono` / `Flux`
- централизованная обработка ошибок
- Flyway-миграция
- unit-тесты для service и controller
- repository integration tests
- профиль `seed` для демонстрационных данных

## Стек

- Kotlin
- Spring Boot WebFlux
- Reactor
- Spring JDBC `JdbcClient`
- PostgreSQL / H2
- Flyway
- JUnit 5 / Mockito / Reactor Test

## Запуск

```bash
gradle bootRun
```

Для PostgreSQL можно задать переменные окружения:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=task_catalog
DB_USER=postgres
DB_PASSWORD=postgres
```

## Тесты

```bash
gradle test
```

## Дополнительно

- [POWERSHELL_USAGE.md](POWERSHELL_USAGE.md) — примеры запуска и проверки через PowerShell
- при необходимости wrapper можно сгенерировать локально командой `gradle wrapper`
