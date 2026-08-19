# VOLT — магазин електроніки на мікросервісах

Навчальний pet-проєкт: інтернет-магазин техніки, розбитий на п'ять незалежних Spring Boot
сервісів, які спілкуються через Kafka та REST, плюс React-фронтенд.

**Стек:** Java 21 · Spring Boot 4.1.0 · Spring Cloud Gateway 2025.1.2 · Spring Security (JWT) ·
PostgreSQL 16 · Apache Kafka 7.5.0 · React 18 + Vite · Docker Compose

---

## Архітектура

```
                        ┌──────────────┐
                        │   frontend   │  :5173  React + Vite (nginx)
                        └──────┬───────┘
                               │ REST
                        ┌──────▼───────┐
                        │ api-gateway  │  :8080  маршрутизація + JWT + CORS
                        └──┬───┬───┬───┘
             ┌─────────────┘   │   └─────────────┐
      ┌──────▼──────┐   ┌──────▼──────┐   ┌──────▼───────────┐
      │auth-service │   │product-svc  │   │  order-service   │
      │   :8081     │   │   :8082     │   │      :8083       │
      └──────┬──────┘   └──────┬──────┘   └────────┬─────────┘
             │                 │                   │
        ┌────▼────┐       ┌────▼─────┐        ┌────▼────┐
        │ auth_db │       │product_db│        │order_db │
        │  :5433  │       │  :5434   │        │  :5435  │
        └─────────┘       └──────────┘        └─────────┘

                    ┌──────────────────────┐
                    │ notification-service │  :8084 → notification_db :5436
                    └──────────────────────┘
```

Кожен сервіс — окремий Maven-проєкт із власною базою даних. Спільної бібліотеки немає
навмисно: DTO подій дублюються в кожному сервісі, тому будь-який з них можна оновити
й задеплоїти незалежно від решти.

### Обмін подіями

```
auth-service   ──user-registered-topic──▶  notification-service   лист про реєстрацію
order-service  ──order-created-topic───▶  notification-service   лист-підтвердження замовлення
                                      └─▶  product-service        списання залишку зі складу
```

`order-service` синхронно ходить у `product-service` (REST) за актуальною ціною й наявністю
під час оформлення, а списання залишку відбувається асинхронно — через подію.

### Структура

```
electronic-store/
├── .env                      # секрети й порти (у .gitignore)
├── .env.example
├── docker/
│   ├── docker-compose.yml        # інфраструктура: Kafka, 4 бази, MailHog
│   ├── docker-compose.app.yml    # застосунки: 5 сервісів + фронтенд
│   ├── auth.Dockerfile
│   ├── product.Dockerfile
│   ├── order.Dockerfile
│   ├── notification.Dockerfile
│   ├── api-gateway.Dockerfile
│   └── frontend.Dockerfile
├── services/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── product-service/
│   ├── order-service/
│   └── notification-service/
└── frontend/
    ├── nginx.conf
    └── .dockerignore
```

Пакети всередині сервісу: `configuration`, `controller`, `dto`, `exception`, `kafka`,
`model`, `repository`, `security`, `service`.

---

## Запуск

### Крок 1. Створити `.env`

```powershell
copy .env.example .env
```

Далі відкрити й підставити свої значення. Мінімум, що треба змінити — `JWT_SECRET`
(рядок від 32 символів, інакше HS256 не прийме ключ).

### Варіант A — усе в Docker

```powershell
cd docker
docker compose --env-file ..\.env -f docker-compose.yml -f docker-compose.app.yml up -d --build
```

Перша збірка триває кілька хвилин: Maven тягне залежності окремо для кожного сервісу.
Далі шари кешуються, повторний запуск — секунди.

Готово, коли `docker ps` показує 14 контейнерів. Сайт: http://localhost:5173

### Варіант B — інфраструктура в Docker, сервіси з IDE

Зручніше під час розробки: не треба перезбирати образ після кожної правки.

```powershell
cd docker
docker compose --env-file ..\.env -f docker-compose.yml up -d
```

Далі в IntelliJ IDEA: встановити плагін **EnvFile**, потім у Run Configuration кожного
сервісу вкладка EnvFile → Enable EnvFile → «+» → вибрати `.env`. Без цього сервіс
не стартує — у `jwt.secret` немає значення за замовчуванням.

Порядок запуску: `auth-service` → `product-service` → `order-service` →
`notification-service` → `api-gateway`. Перші два створюють топіки Kafka при старті.

Фронтенд окремо:

```powershell
cd frontend
npm install
npm run dev
```

### Корисні адреси

| Що | Де |
|---|---|
| Магазин | http://localhost:5173 |
| API (через gateway) | http://localhost:8080 |
| Пошта (MailHog) | http://localhost:8025 |
| Kafka UI | http://localhost:8090 |

---

## Ролі та адмінка

При реєстрації користувач отримує роль `USER`. Роль `ADMIN` призначається вручну в базі:

```powershell
docker exec -it shop-auth-db psql -U auth_user -d auth_db -c "UPDATE users SET role = 'ADMIN' WHERE email = 'ваша@пошта';"
```

Після цього треба **вийти й залогінитися знову** — роль зашита всередину JWT, і старий
токен про неї не знає.

В адмінці: додавання товарів і зміна статусів замовлень. Категорії через UI не створюються,
тільки через API:

```powershell
curl.exe -X POST http://localhost:8080/api/categories `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer ТОКЕН" `
  -d "{\"name\":\"Телевізори\",\"slug\":\"tv\",\"description\":\"ТВ та проектори\"}"
```

Токен можна взяти з браузера: F12 → Application → Local Storage → ключ `volt.accessToken`.

При першому старті `product-service` засіває каталог трьома категоріями й шістьма товарами —
але лише якщо таблиця категорій порожня.

---

## API

Усі запити йдуть через gateway на `http://localhost:8080`.

| Метод | Шлях | Доступ |
|---|---|---|
| POST | `/api/auth/register` | публічний |
| POST | `/api/auth/login` | публічний |
| POST | `/api/auth/refresh` | публічний |
| GET | `/api/auth/me` | авторизований |
| GET | `/api/auth/users` | ADMIN |
| GET | `/api/products`, `/api/products/{id}` | публічний |
| POST / PUT / DELETE | `/api/products` | ADMIN |
| GET | `/api/categories`, `/api/categories/{id}` | публічний |
| POST / PUT / DELETE | `/api/categories` | ADMIN |
| POST | `/api/orders` | авторизований |
| GET | `/api/orders/my`, `/api/orders/{id}` | власник або ADMIN |
| GET | `/api/orders/all` | ADMIN |
| PUT | `/api/orders/{id}/status` | ADMIN |
| GET | `/api/notifications` | ADMIN |

Параметри каталогу: `?search=&categoryId=&brand=&minPrice=&maxPrice=&page=&size=&sortBy=&direction=`

Приклад реєстрації:

```powershell
curl.exe -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d "{\"firstName\":\"Андрій\",\"lastName\":\"Сорока\",\"email\":\"test@test.com\",\"password\":\"123456\"}"
```

---

## Безпека

- `auth-service` видає пару токенів: access (15 хв) і refresh (7 днів), алгоритм HS256.
  Паролі зберігаються як BCrypt-хеш.
- Gateway відсікає неавторизовані запити на закриті маршрути й додає до проксійованого
  запиту заголовки `X-User-Id` та `X-User-Role`.
- Кожен сервіс **додатково перевіряє підпис токена сам** — власним `JwtAuthenticationFilter`.
  Прямий запит повз gateway на `:8082` без валідного токена теж не пройде.
- CORS налаштований на рівні gateway. Це важливо: Spring Cloud Gateway обробляє preflight
  `OPTIONS` самостійно й не проксіює його далі, тому конфігурації в downstream-сервісах
  для preflight недостатньо. `DedupeResponseHeader` прибирає задвоєння заголовків
  на звичайних запитах.

---

## Змінні оточення

| Група | Змінні |
|---|---|
| JWT | `JWT_SECRET`, `JWT_ACCESS_EXPIRATION`, `JWT_REFRESH_EXPIRATION` |
| Бази | `AUTH_DB_*`, `PRODUCT_DB_*`, `ORDER_DB_*`, `NOTIFICATION_DB_*` (`_HOST`, `_PORT`, `_NAME`, `_USER`, `_PASSWORD`) |
| Kafka | `KAFKA_BOOTSTRAP_SERVERS` |
| Пошта | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_TLS` |
| Сервіси | `PRODUCT_SERVICE_URL`, `FRONTEND_URL` |

У `.env` усі адреси вказують на `localhost` — це для запуску з IDE. Всередині мережі Docker
контейнери звертаються один до одного за іменами сервісів, тому `docker-compose.app.yml`
перевизначає ці змінні (`AUTH_DB_HOST: auth-db`, `KAFKA_BOOTSTRAP_SERVERS: kafka:29092`).

За замовчуванням пошта йде в MailHog. Для реального Gmail треба прибрати рядки `MAIL_HOST`
і `MAIL_PORT` з `docker-compose.app.yml` та вказати в `.env` `smtp.gmail.com:587`,
`MAIL_SMTP_AUTH=true`, `MAIL_SMTP_TLS=true` і App Password (не звичайний пароль від акаунта).

---

## Часті проблеми

**`Connection to localhost:5433 refused`** — контейнер бази не піднявся. Найчастіше `.env`
не знайдено: compose шукає його поруч із собою, тому потрібен явний `--env-file ..\.env`.
Перевірити, що файл справді називається `.env`, а не `.env.txt`.

**`Only one usage of each socket address`** — порт зайнятий. Сервіс досі працює в IntelliJ
або лишився висіти процес: `netstat -ano | findstr ":8082"`, далі `taskkill /PID <номер> /F`.

**`blocked by CORS policy`** у консолі браузера — origin не збігається з `FRONTEND_URL`.
Зазвичай Vite зайняв 5174 замість 5173. Лікується `strictPort: true` у `vite.config.js`,
щоб Vite падав з помилкою замість тихої зміни порту.

**`function lower(bytea) does not exist`** — коли параметр фільтра приходить `null`, драйвер
не знає його типу й шле як `bytea`. Вирішено через `CAST(:param AS String)` у JPQL-запиті
`ProductRepository`.

**`npm : running scripts is disabled on this system`** — політика PowerShell, не помилка Node.
`Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned` або запуск через `npm.cmd`.

**`UNKNOWN_TOPIC_OR_PARTITION`** у логах notification-service — топіки ще не створені.
Вони з'являються при старті `auth-service` і `order-service`, тому consumer'и краще
піднімати після producer'ів.

---

## Команди

```powershell
# логи сервіса
docker logs -f shop-product-service

# перезібрати один сервіс після правки коду
docker compose --env-file ..\.env -f docker-compose.yml -f docker-compose.app.yml up -d --build product-service

# зупинити все
docker compose --env-file ..\.env -f docker-compose.yml -f docker-compose.app.yml down

# зупинити і стерти дані баз
docker compose --env-file ..\.env -f docker-compose.yml -f docker-compose.app.yml down -v

# підключитися до бази
docker exec -it shop-product-db psql -U product_user -d product_db
```

---

## Плани

- `cart-service` — зараз кошик живе в localStorage браузера
- `payment-service` і вебхуки платіжної системи
- Eureka або Consul замість статичних URL у gateway
- Redis для кешу каталогу
- Тести: `@DataJpaTest` для репозиторіїв, Testcontainers для інтеграційних
- CI: збірка образів і прогін тестів на push
