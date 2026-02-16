# Explore With Me

Микросервисное приложение для поиска событий и участия в них. Пользователи могут создавать события, подавать заявки на участие, а администраторы — модерировать контент и составлять тематические подборки.

## Архитектура

Проект состоит из двух независимых сервисов, каждый со своей базой данных PostgreSQL:

```
┌────────────────────┐         REST          ┌────────────────────┐
│                    │ ──────────────────────▶│                    │
│   Main Service     │    GET /stats         │   Stats Service    │
│   (порт 8080)      │    POST /hit          │   (порт 9090)      │
│                    │◀──────────────────────│                    │
└────────┬───────────┘                       └────────┬───────────┘
         │                                            │
         ▼                                            ▼
┌────────────────────┐                       ┌────────────────────┐
│   PostgreSQL       │                       │   PostgreSQL       │
│   ewm-main :5433   │                       │   ewm-stats :5432  │
└────────────────────┘                       └────────────────────┘
```

**Main Service** — основная бизнес-логика: управление событиями, пользователями, категориями, подборками и заявками на участие.

**Stats Service** — сбор и отдача статистики просмотров эндпоинтов. Разделён на три Maven-подмодуля:
- `dto` — общие DTO для межсервисного взаимодействия
- `client` — REST-клиент (`StatClient`), подключаемый как зависимость в main-service
- `server` — REST API сервиса статистики

## Стек технологий

- **Java 21**, **Spring Boot 3.3.2**
- **Spring Data JPA** + **Hibernate** + **JPA Specifications** (динамические запросы)
- **PostgreSQL 16.1**
- **Docker** / **Docker Compose**
- **Maven** (многомодульная сборка)
- **Lombok**, **Jakarta Bean Validation**
- **Checkstyle**, **SpotBugs**, **JaCoCo**

## API

### Main Service

Три уровня доступа к API:

| Уровень | Префикс | Описание |
|---------|---------|----------|
| Публичный | `/events`, `/categories`, `/compilations` | Поиск и просмотр событий, категорий, подборок |
| Приватный | `/users/{userId}/events`, `/users/{userId}/requests` | Создание событий, управление своими заявками |
| Админский | `/admin/users`, `/admin/events`, `/admin/categories`, `/admin/compilations` | Модерация событий, управление пользователями и категориями |

### Stats Service

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| `POST` | `/hit` | Сохранить факт обращения к эндпоинту |
| `GET` | `/stats` | Получить статистику просмотров с фильтрацией по дате, URI и уникальности IP |

Полные спецификации API: [`ewm-main-service-spec.json`](ewm-main-service-spec.json), [`ewm-stats-service-spec.json`](ewm-stats-service-spec.json)

## Схема базы данных

### Main Service

```
users ──────────┐
                ▼
categories ──▶ events ◀── locations
                │
                ▼
       participation_requests

compilations ◀──▶ events  (M:N через compilation_events)
```

Основные таблицы: `users`, `categories`, `locations`, `events`, `participation_requests`, `compilations`, `compilation_events`.

### Stats Service

Единственная таблица `endpoint_hit` с индексами по `timestamp` и составным `(uri, timestamp)`.

## Запуск

### Docker Compose (рекомендуется)

```bash
mvn clean package -DskipTests
docker-compose up -d
```

После запуска:
- Main Service: http://localhost:8080
- Stats Service: http://localhost:9090

### Локально

Требуется два запущенных экземпляра PostgreSQL (порты 5432 и 5433).

```bash
mvn clean install
```

## Особенности реализации

- **JPA Specifications** — динамическое построение запросов для поиска событий с множеством необязательных фильтров (текст, категории, платность, доступность, диапазон дат)
- **EventEnricher** — компонент, агрегирующий данные из нескольких источников (количество подтверждённых заявок из БД + количество просмотров из stats-service) и обогащающий DTO событий
- **Конечный автомат состояний** — события проходят через состояния `PENDING → PUBLISHED / CANCELED`, заявки — через `PENDING → CONFIRMED / REJECTED / CANCELED`, переходы валидируются в сервисном слое
- **Межсервисная коммуникация** — main-service подключает `stats-client` как Maven-зависимость и через `RestClient` обращается к stats-service
- **SQL-схемы** — миграции через `schema.sql`, Hibernate DDL auto отключён
