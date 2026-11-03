# HotelAPI

RESTful API для работы с отелями: список, детальная информация, поиск по фильтрам, создание, добавление удобств и гистограммы по параметрам.

## Архитектура

- **Controller:** принимает запрос, валидирует входные данные, отдаёт DTO.
    
- **Service:** бизнес-логика, транзакции, маппинг Entity в DTO через `MapStruct` (пакет `mapper`).
    
- **Repository:** 
	  
	- JPA Repository (`HotelRepository`) - базовые CRUD + JPQL-запрос для гистограммы по `amenities` (возвращает `List<AmenityCountProjection>`, в `Map` собирает сервис).

	- Два кастомных репозитория на Criteria API:

		- `HotelFilterRepositoryImpl` - динамический поиск по фильтрам.
		- `HotelHistogramRepositoryImpl` - вывод гистограммы через группировку по `brand`/`city`/`country`.
        
- **Entity:** `Hotel` с `@Embedded` (`Address`, `Contacts`, `ArrivalTime`) и `@ElementCollection` (`amenities`).
    
- **DTO:** 

	- `HotelShortInfoReadDto` (для `/hotels`, `/search` и ответа `POST /hotels`), `HotelReadDto` (для `/hotels/{id}`), `HotelCreateDto`.
	- `ErrorResponse` для формирования ответа об ошибках в `@RestControllerAdvice`.
    
- **Liquibase:** `db/changelog/changelog-1.yaml` для управления схемой БД и переносимости.

## Стек технологий

- Java 21, Spring Boot 3.5.16
- Spring Web, Spring Data JPA, Spring Validation
- H2 (in-memory), Liquibase
- MapStruct (маппинг Entity в DTO), Lombok
- SpringDoc OpenAPI (Swagger UI)
- JUnit 5, Mockito, AssertJ, MockMvc
- Maven

## Быстрый запуск

```bash
# 1. Склонировать репозиторий
git clone https://github.com/Egrius/HotelAPI

# 2. Перейти в папку проекта (если не было git clone <url> .)
cd HotelAPI

# 3. Запустить командой
mvn spring-boot:run
````

После запуска:

- API: [http://localhost:8092/property-view/hotels](http://localhost:8092/property-view/hotels)
- Swagger UI: [http://localhost:8092/swagger-ui.html](http://localhost:8092/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8092/v3/api-docs](http://localhost:8092/v3/api-docs)
- H2 Console: [http://localhost:8092/h2-console](http://localhost:8092/h2-console) (JDBC URL: `jdbc:h2:mem:testdb`, user: `demo`, password: `demo`)

База данных - H2 in-memory. Схема создаётся через Liquibase при старте, при перезапуске данные сбрасываются.

## Что сделано

- Все 6 эндпоинтов из ТЗ с общим префиксом `/property-view`
- Разделение на слои: контроллер, сервис, репозиторий. DTO отдельно от Entity, маппинг через MapStruct
- Валидация входных данных (`@Valid`, `@NotBlank`, `@NotEmpty`, `@Size`, `@NotNull`, `@Positive`, `@Email`)
- Глобальная обработка ошибок через `@RestControllerAdvice`
- Документация API через Swagger (`springdoc-openapi`)
- Liquibase-миграции (YAML) и платформонезависимые запросы (JPQL + Criteria API) для быстрого переключения с H2 на другую СУБД (PostgreSQL, MySQL)

## Тесты

```Bash
mvn test
```

Находятся по пути `src/test/java/by/Egrius/demoTask`:

- **Unit (`/unit`):** `HotelServiceUnitTests` - Mockito, бизнес-логика сервиса, ветвления.
    
- **Интеграционные (`/integration`):**
    
    - `HotelHistogramRepositoryIntegrationTests`
    - `HotelFilterRepositoryIntegrationTests`
    - `HotelRepositoryIntegrationTests`
    - `HotelServiceIntegrationTests`
    
    Реальная H2 + Liquibase, проверка корректности выполнения кастомных запросов и работы сервиса с БД.
    
- **Slice-тесты (`/slice`):**
    
    - `ApiControllerTests` - `@WebMvcTest` на проверку HTTP-контракта: статусы 200/400/404, формат JSON, валидация, работа `@RestControllerAdvice`.

## Нюансы реализации

- **Criteria API для `brand`/`city`/`country`, JPQL для `amenities`.**  
    Для первых нужен динамический выбор пути (`brand` - поле `Hotel`, `city`/`country` - внутри `@Embedded Address`),
    а в JPQL имя поля нельзя параметризовать (`GROUP BY :param` воспримется как литерал).
    
    Criteria API с `Map<String, Function<Root<Hotel>, Path<String>>>` решает это без дублирования кода. \
    Для `amenities` JPQL проще и нагляднее: `SELECT a AS amenity, COUNT(h) AS count FROM Hotel h JOIN h.amenities a GROUP BY a ORDER BY a` - переносимый запрос без динамики,
    сразу мапится в `List<AmenityCountProjection>`.
    
- **`@ElementCollection` для amenities, а не `@OneToMany` к `Amenity`.**  
    В ТЗ нет эндпоинта на создание отдельного удобства и нет дополнительных полей у удобства.
    `@ElementCollection` создаёт таблицу `hotel_amenities (hotel_id, amenity)`, а Hibernate управляет строками через `Set<String>`.
    Если бы появился справочник удобств - вынес бы в `@Entity` + `@ManyToMany`.
    
    В Liquibase добавлен составной `PK (hotel_id, amenity)` для `hotel_amenities` для исключения дублей в рамках одного отеля на уровне БД.
    
- **`ddl-auto: validate`, а не `create-drop`/`update`.**  
    Схему создаёт только Liquibase. Hibernate сверяет `@Entity` с фактической схемой и падает при рассинхроне.
    Это гарантирует, что тесты и реальное приложение используют одну и ту же схему из changelog.
    
- **Кастомное `HotelNotFoundException` вместо `EntityNotFoundException`.**  
    Собственное бизнес-исключение `HotelNotFoundException` для явного отлова в `@RestControllerAdvice` и возврата `404 Not Found`.
    
- **Один `findAllByFilter(HotelFilter)` с динамическими предикатами, а не N методов.**  
    Criteria API с `List<Predicate>` собирает только переданные (не-null) фильтры и комбинирует их через `AND` без необходимости плодить комбинаторные методы в репозитории.
  Фильтрация по списку `amenities` реализована через коррелированный `EXISTS`-подзапрос с `GROUP BY` и `HAVING COUNT`, чтобы находить отели, содержащие
  ВСЕ запрошенные удобства (без учёта регистра).
