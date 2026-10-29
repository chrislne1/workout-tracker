# Overload: Workout Tracker & Progressive Overload Analytics PWA

Spring Boot 3 / Java 17 backend, React + TypeScript + Tailwind PWA frontend, PostgreSQL with Flyway.

## Run locally (no database install needed)
```bash
# Backend (H2 in PostgreSQL mode, seeded by Flyway)
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev
#   Swagger UI: http://localhost:8080/swagger-ui.html

# Frontend (proxies /api to :8080)
cd frontend && npm install && npm run dev      # http://localhost:5173
```
Or the whole stack with Postgres: `docker compose up --build` then open http://localhost:3000.

## Tests
`cd backend && mvn test` runs JUnit 5 + Mockito unit tests (1RM math, volume, regression threshold, analytics, trend exclusion) and `@WebMvcTest` tests (regression endpoint, validation, 404/400/500 problem-details).

## Deploy for $0
* **Database:** create a Neon or Supabase Postgres. Flyway runs the migrations on first boot.
* **Backend (Render, Docker):** root dir `backend`. Env vars: `DATABASE_URL` (JDBC form, e.g. `jdbc:postgresql://HOST/DB?sslmode=require`), `DATABASE_USER`, `DATABASE_PASSWORD`, `CORS_ORIGINS` (your frontend URL).
* **Frontend (Vercel):** root dir `frontend`, env `VITE_API_URL=https://<your-render-service>.onrender.com`.
* On iPhone: open the site in Safari, Share, Add to Home Screen.

## Notes
* Single seeded demo user (`app.default-user-id`); authentication was not part of the spec.
* Warm-up sets are excluded from every volume, 1RM, PR and trend calculation.
* The >10% regression check runs when you tap **Finish exercise** (so a half-finished exercise never triggers it): `GET /api/sessions/{id}/regression?variantId=`.
* Sessions tagged `PURPOSEFUL_DELOAD` or `INJURY_DISCOMFORT` stay in history but are left out of the progression-velocity trendline.
