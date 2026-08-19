# VOLT — Electronics Store on Microservices

A learning pet project: an online electronics store split into five independent Spring Boot
services communicating over Kafka and REST, plus a React frontend.

**Stack:** Java 21 · Spring Boot 4.1.0 · Spring Cloud Gateway 2025.1.2 · Spring Security (JWT) ·
PostgreSQL 16 · Apache Kafka 7.5.0 · React 18 + Vite · Docker Compose

---

## Architecture

```
                        ┌──────────────┐
                        │   frontend   │  :5173  React + Vite (nginx)
                        └──────┬───────┘
                               │ REST
                        ┌──────▼───────┐
                        │ api-gateway  │  :8080  routing + JWT + CORS
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

Every service is a standalone Maven project with its own database. There is deliberately no
shared library: event DTOs are duplicated in each service, so any of them can be updated and
deployed independently of the rest.

### Event flow

```
auth-service   ──user-registered-topic──▶  notification-service   registration email
order-service  ──order-created-topic───▶  notification-service   order confirmation email
                                      └─▶  product-service        stock decrement
```

During checkout `order-service` calls `product-service` synchronously (REST) for the current
price and availability, while the stock decrement happens asynchronously via an event.

### Project layout

```
electronic-store/
├── .env                      # secrets and ports (gitignored)
├── .env.example
├── docker/
│   ├── docker-compose.yml        # infrastructure: Kafka, 4 databases, MailHog
│   ├── docker-compose.app.yml    # applications: 5 services + frontend
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

Packages inside each service: `configuration`, `controller`, `dto`, `exception`, `kafka`,
`model`, `repository`, `security`, `service`.

---

## Getting started

### Step 1. Create `.env`

```powershell
copy .env.example .env
```

Then open it and fill in your values. The one thing you must change is `JWT_SECRET`
(at least 32 characters, otherwise HS256 will reject the key).

### Option A — everything in Docker

```powershell
cd docker
docker compose --env-file ..\.env -f docker-compose.yml -f docker-compose.app.yml up -d --build
```

The first build takes a few minutes: Maven downloads dependencies separately for each service.
After that the layers are cached and subsequent starts take seconds.

You are ready when `docker ps` lists 14 containers. Open http://localhost:5173

### Option B — infrastructure in Docker, services from the IDE

More convenient while developing: no image rebuild after every code change.

```powershell
cd docker
docker compose --env-file ..\.env -f docker-compose.yml up -d
```

Then in IntelliJ IDEA install the **EnvFile** plugin, and for each service go to
Run Configuration → EnvFile tab → Enable EnvFile → "+" → select `.env`. Without this the
service will not start, since `jwt.secret` has no default value.

Startup order: `auth-service` → `product-service` → `order-service` →
`notification-service` → `api-gateway`. The first two create the Kafka topics on startup.

Frontend separately:

```powershell
cd frontend
npm install
npm run dev
```

### Endpoints

| What | Where |
|---|---|
| Storefront | http://localhost:5173 |
| API (through the gateway) | http://localhost:8080 |
| Mail catcher (MailHog) | http://localhost:8025 |
| Kafka UI | http://localhost:8090 |

---

## Roles and the admin panel

New users get the `USER` role on registration. The `ADMIN` role is granted manually in the database:

```powershell
docker exec -it shop-auth-db psql -U auth_user -d auth_db -c "UPDATE users SET role = 'ADMIN' WHERE email = 'your@email';"
```

After that you must **log out and log in again** — the role is embedded in the JWT, and the old
token knows nothing about it.

The admin panel supports adding products and changing order statuses. Categories are not
exposed in the UI, only through the API:

```powershell
curl.exe -X POST http://localhost:8080/api/categories `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer TOKEN" `
  -d "{\"name\":\"TVs\",\"slug\":\"tv\",\"description\":\"TVs and projectors\"}"
```

You can grab the token from the browser: F12 → Application → Local Storage → key `volt.accessToken`.

On first startup `product-service` seeds the catalog with three categories and six products —
but only when the categories table is empty.

---

## API

All requests go through the gateway at `http://localhost:8080`.

| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register` | public |
| POST | `/api/auth/login` | public |
| POST | `/api/auth/refresh` | public |
| GET | `/api/auth/me` | authenticated |
| GET | `/api/auth/users` | ADMIN |
| GET | `/api/products`, `/api/products/{id}` | public |
| POST / PUT / DELETE | `/api/products` | ADMIN |
| GET | `/api/categories`, `/api/categories/{id}` | public |
| POST / PUT / DELETE | `/api/categories` | ADMIN |
| POST | `/api/orders` | authenticated |
| GET | `/api/orders/my`, `/api/orders/{id}` | owner or ADMIN |
| GET | `/api/orders/all` | ADMIN |
| PUT | `/api/orders/{id}/status` | ADMIN |
| GET | `/api/notifications` | ADMIN |

Catalog query parameters: `?search=&categoryId=&brand=&minPrice=&maxPrice=&page=&size=&sortBy=&direction=`

Registration example:

```powershell
curl.exe -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d "{\"firstName\":\"Andrii\",\"lastName\":\"Soroka\",\"email\":\"test@test.com\",\"password\":\"123456\"}"
```

---

## Security

- `auth-service` issues a token pair: access (15 min) and refresh (7 days), signed with HS256.
  Passwords are stored as BCrypt hashes.
- The gateway rejects unauthenticated requests to protected routes and adds `X-User-Id` and
  `X-User-Role` headers to the proxied request.
- Every service **additionally verifies the token signature itself** through its own
  `JwtAuthenticationFilter`. A direct request to `:8082` bypassing the gateway will not get
  through without a valid token either.
- CORS is configured at the gateway level. This matters: Spring Cloud Gateway handles the
  `OPTIONS` preflight itself and does not proxy it downstream, so configuring CORS only in the
  downstream services is not enough. `DedupeResponseHeader` removes duplicated headers on
  regular requests.

---

## Environment variables

| Group | Variables |
|---|---|
| JWT | `JWT_SECRET`, `JWT_ACCESS_EXPIRATION`, `JWT_REFRESH_EXPIRATION` |
| Databases | `AUTH_DB_*`, `PRODUCT_DB_*`, `ORDER_DB_*`, `NOTIFICATION_DB_*` (`_HOST`, `_PORT`, `_NAME`, `_USER`, `_PASSWORD`) |
| Kafka | `KAFKA_BOOTSTRAP_SERVERS` |
| Mail | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_TLS` |
| Services | `PRODUCT_SERVICE_URL`, `FRONTEND_URL` |

In `.env` every address points to `localhost` — that is meant for running from the IDE. Inside
the Docker network containers reach each other by service name, so `docker-compose.app.yml`
overrides these variables (`AUTH_DB_HOST: auth-db`, `KAFKA_BOOTSTRAP_SERVERS: kafka:29092`).

By default mail goes to MailHog. For real Gmail, remove the `MAIL_HOST` and `MAIL_PORT` lines
from `docker-compose.app.yml` and set `smtp.gmail.com:587`, `MAIL_SMTP_AUTH=true`,
`MAIL_SMTP_TLS=true` and an App Password (not your regular account password) in `.env`.

---

## Troubleshooting

**`Connection to localhost:5433 refused`** — the database container is not running. Usually
`.env` was not found: compose looks for it next to itself, so an explicit `--env-file ..\.env`
is required. Also make sure the file is really named `.env` and not `.env.txt`.

**`Only one usage of each socket address`** — the port is taken. Either the service is still
running in IntelliJ or a process is left hanging: `netstat -ano | findstr ":8082"`, then
`taskkill /PID <number> /F`.

**`blocked by CORS policy`** in the browser console — the origin does not match `FRONTEND_URL`.
Usually Vite grabbed 5174 instead of 5173. Fix it with `strictPort: true` in `vite.config.js`
so Vite fails loudly instead of silently switching ports.

**`function lower(bytea) does not exist`** — when a filter parameter arrives as `null`, the
driver does not know its type and sends it as `bytea`. Solved with `CAST(:param AS String)`
in the `ProductRepository` JPQL query.

**`npm : running scripts is disabled on this system`** — a PowerShell policy, not a Node error.
Run `Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned` or use `npm.cmd`.

**`UNKNOWN_TOPIC_OR_PARTITION`** in the notification-service logs — the topics do not exist yet.
They are created when `auth-service` and `order-service` start, so consumers are best started
after the producers.

---

## Commands

```powershell
# service logs
docker logs -f shop-product-service

# rebuild a single service after a code change
docker compose --env-file ..\.env -f docker-compose.yml -f docker-compose.app.yml up -d --build product-service

# stop everything
docker compose --env-file ..\.env -f docker-compose.yml -f docker-compose.app.yml down

# stop and wipe database volumes
docker compose --env-file ..\.env -f docker-compose.yml -f docker-compose.app.yml down -v

# connect to a database
docker exec -it shop-product-db psql -U product_user -d product_db
```

---

## Roadmap

- `cart-service` — the cart currently lives in browser localStorage
- `payment-service` and payment provider webhooks
- Eureka or Consul instead of static URLs in the gateway
- Redis for catalog caching
- Tests: `@DataJpaTest` for repositories, Testcontainers for integration tests
- CI: build images and run tests on push