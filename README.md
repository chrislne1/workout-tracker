# Overload

A mobile-first workout tracker (PWA) with progressive overload analytics and muscle volume distribution.

## Features
- Log sets with estimated 1RM (Epley), per-set deltas vs. your last session, and PR alerts
- Warm-up sets excluded from all strength and volume analytics
- Regression prompt when volume drops more than 10% below your 30-day average
- Deload and injury sessions left out of the strength trendline
- Muscle volume and weekly hard sets over 1M, 3M, 6M, 1Y and all-time windows
- Push:pull and agonist:antagonist ratios
- Installable on iPhone, with the exercise list cached for offline use

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17+, Spring Boot 3.3, Spring Data JPA, Spring Validation |
| Database | PostgreSQL (Neon / Supabase), H2 for local dev, Flyway migrations |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS, PWA (service worker + manifest) |
| Testing | JUnit 5, Mockito, `@WebMvcTest` |
| API docs | Springdoc OpenAPI / Swagger UI |
| DevOps | Docker, Docker Compose, GitHub Actions, Render (backend), Vercel (frontend) |

## Run Locally

Requirements: JDK 21 (or 17), Maven, Node.js 20+

```bash
# Terminal 1: backend (http://localhost:8080)
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

# Terminal 2: frontend (http://localhost:5173)
cd frontend
npm install        # first time only
npm run dev
```

- API docs: http://localhost:8080/swagger-ui.html
- Backend tests: `cd backend && mvn test`
- Full stack with Postgres: `docker compose up --build`, then open http://localhost:3000

## Project Structure
```
backend/    Spring Boot API, Flyway SQL migrations, tests
frontend/   React PWA
docker-compose.yml
.github/workflows/ci.yml
```

## Deploy
1. Create a Postgres database on Neon or Supabase.
2. Deploy `backend` to Render (Docker) with `DATABASE_URL` (JDBC format), `DATABASE_USER`, `DATABASE_PASSWORD`, `CORS_ORIGINS`.
3. Deploy `frontend` to Vercel with `VITE_API_URL` set to the Render URL.
4. On iPhone: open in Safari, Share, Add to Home Screen.
