# GridMind

GridMind is a workshop inventory and hardware management project.

## Prerequisites

- Java 21
- Node.js 22+
- PostgreSQL 17 (or Docker)

## Quick start

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Start PostgreSQL with Docker:
   ```bash
   docker compose up -d postgres
   ```

3. Start the backend:
   ```bash
   cd backend
   ./gradlew bootRun
   ```

4. Start the frontend:
   ```bash
   cd frontend
   npm install
   npm run dev -- --host 0.0.0.0
   ```

5. Open the app at http://localhost:5173

## Backend API

- GET /api/inventory/items
- POST /api/inventory/items
- PATCH /api/inventory/items/{id}/quantity

## Project structure

- backend/src/main/kotlin/org/gridmind/backend/inventory
- backend/src/main/resources/db/migration
- frontend/src

## Notes

The backend is intentionally structured around a modular monolith with a clear split between domain, application, persistence, and HTTP layers.
