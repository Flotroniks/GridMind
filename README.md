# GridMind

GridMind is a smart inventory and workshop management application for makers — track hardware, organize where it's physically stored, and (eventually) plan projects around it.

## Stack

- **Backend**: Kotlin, Spring Boot, PostgreSQL, Flyway migrations
- **Frontend**: React, TypeScript, Vite, Material UI (MUI), TanStack Query, react-i18next
- **Dev environment**: Docker Compose (Postgres, backend, frontend, all with live reload)

## Prerequisites

- Docker and Docker Compose (recommended — see Quick start)
- Or, to run without Docker: Java 21, Node.js 22+, PostgreSQL 17

## Quick start (Docker)

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Start everything:
   ```bash
   docker compose up
   ```

3. Open the app at http://localhost:5173

The backend runs on http://localhost:8080 and reloads on code changes; the frontend dev server does the same.

## Quick start (without Docker)

1. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```

2. Start PostgreSQL with Docker (or point `.env` at your own instance):
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

## Features

- **Inventory**: create, edit, delete, and search hardware items — manufacturer, reference, description, tags, notes, product/datasheet URLs, minimum stock quantity
- **Categories**: data-driven, user-created (not hardcoded)
- **Item status**: mark items in service or out of service (HS), and filter them out of the active view
- **Storage**: hierarchical storage locations (e.g. Workshop → Drawer → Bin), with an item's stock spread across multiple locations and moved between them atomically

## Backend API

- `GET/POST /api/inventory/items`, `GET/PATCH/DELETE /api/inventory/items/{id}` — inventory items (`search`, `categoryId`, `manufacturer`, `status` query filters)
- `GET/POST /api/categories` — categories
- `GET/POST/DELETE /api/storage/locations`, `GET /api/storage/locations/{id}/contents` — storage hierarchy
- `GET /api/storage/items/{itemId}/stock`, `POST /api/storage/stock/allocate`, `POST /api/storage/stock/move` — stock allocation and movement

## Project structure

```text
backend/src/main/kotlin/org/gridmind/backend/
├── inventory/    # items: domain, application, api, persistence
├── category/     # categories
├── storage/      # storage locations and stock allocation
└── shared/       # cross-cutting config and error handling

backend/src/main/resources/db/migration/   # Flyway migrations

frontend/src/
├── app/                 # router, root component
├── features/
│   ├── inventory/
│   ├── categories/
│   └── storage/
├── components/common/   # shared UI (toasts, theme toggle, language switcher, error boundary)
├── layouts/
└── theme/               # MUI theme + light/dark color mode
```

## Notes

The backend is intentionally structured as a modular monolith with a clear split between domain, application, persistence, and HTTP layers per feature module. See `ROADMAP.md` for the planned direction (Gridfinity modeling, projects/BOM, AI-assisted import, and more).
