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
- **Quantity breakdown**: an item's quantity splits into ready-to-use, in-use (e.g. tied up in a project), and HS (broken) — the list shows what's actually available, not just the raw total
- **Storage**: hierarchical storage locations (e.g. Workshop → Drawer → Bin), with an item's stock spread across multiple locations and moved between them atomically
- **Product search**: look up a part by name or MPN across external catalogs (DigiKey, Mouser) instead of typing everything by hand — see [Product catalog providers](#product-catalog-providers)
- **Local image storage**: an image picked during product search is downloaded once and kept on disk, independent of the provider or an internet connection

## Backend API

- `GET/POST /api/inventory/items`, `GET/PATCH/DELETE /api/inventory/items/{id}` — inventory items (`search`, `categoryId`, `manufacturer` query filters)
- `GET/POST /api/categories` — categories
- `GET/POST/DELETE /api/storage/locations`, `GET /api/storage/locations/{id}/contents` — storage hierarchy
- `GET /api/storage/items/{itemId}/stock`, `POST /api/storage/stock/allocate`, `POST /api/storage/stock/move` — stock allocation and movement
- `GET /api/catalog/search?query=` — search configured product catalog providers, grouped and normalized (see below)
- `GET /api/media/{id}` — serves a locally stored image by id

## Project structure

```text
backend/src/main/kotlin/org/gridmind/backend/
├── inventory/    # items: domain, application, api, persistence (incl. local image storage)
├── category/     # categories
├── storage/      # storage locations and stock allocation
├── catalog/      # external product search: domain, application, api, provider adapters
└── shared/       # cross-cutting config and error handling

backend/src/main/resources/db/migration/   # Flyway migrations

frontend/src/
├── app/                 # router, root component
├── features/
│   ├── inventory/
│   ├── categories/
│   ├── storage/
│   └── catalog/         # product search UI (search step + confirmation step)
├── components/common/   # shared UI (toasts, theme toggle, language switcher, error boundary)
├── layouts/
└── theme/               # MUI theme + light/dark color mode
```

## Product catalog providers

When adding an inventory item, GridMind can look it up across external parts catalogs instead of requiring everything to be typed in by hand — search a name or MPN, pick the right result, review/edit before saving. Each provider is an independent, optional integration: GridMind works with zero, one, or several configured at once.

| Provider | Type | Status | Authentication | Images | Best for |
| --- | --- | --- | --- | --- | --- |
| [DigiKey](https://developer.digikey.com) | Electronic components | **Supported** | OAuth2 (client credentials) | Yes | General electronic components with a real MPN |
| [Mouser](https://www.mouser.com/api-hub/) | Electronic components | **Supported**¹ | API key | Yes | Second source, used to validate cross-provider grouping |
| [Octopart / Nexar](https://nexar.com/api) | Electronic components aggregator | **Rejected** | OAuth2 | Yes | — its terms cap caching at 24h and forbid self-hosting images, which conflicts directly with this project's "keep images locally, indefinitely" goal |
| [partsdb.io](https://www.partsdb.io/) | Electronic components (reichelt/Conrad, EU-focused) | **Planned** | API key | Not confirmed in its docs | Low-friction fallback (plain key, no OAuth, permissive terms) if DigiKey/Mouser coverage is ever insufficient |
| Maker/dev-board catalogs (Adafruit, SparkFun, ...) | Maker hardware | **Planned** | Not researched | Not researched | Breakout boards and dev boards — DigiKey/Mouser already surface some of these (e.g. searching "BME280" returns SparkFun and Adafruit boards), but a dedicated maker catalog isn't implemented |

¹ Code is implemented and its mapping is validated against Mouser's real API schema, but Mouser reviews every API key request manually (1-2 business days) — a freshly issued key won't return results until it's approved on Mouser's side. Nothing on the GridMind side needs to change once that happens.

Each supported provider in detail:

**DigiKey** — Product Information API v4, official API. Requires `DIGIKEY_CLIENT_ID` and `DIGIKEY_CLIENT_SECRET` (get them at [developer.digikey.com](https://developer.digikey.com): create an app, select the "Product Information V4" product). Free tier: 1000 requests/day. Returns name, manufacturer, MPN, description, category, datasheet URL, and an image. Note: an app's OAuth credentials are tied to the environment it was created for — a "Production" app's credentials are rejected by the sandbox host and vice versa; `DIGIKEY_BASE_URL` (default: production) lets you switch if needed.

**Mouser** — Search API v1, official API. Requires `MOUSER_API_KEY` (apply at [mouser.com/api-hub](https://www.mouser.com/en/api-hub/), Search API section — manual approval, 1-2 business days). Free tier: 1000 requests/day, 30/minute. Same data shape as DigiKey (name, manufacturer, MPN, description, category, datasheet, image).

### Provider configuration

```env
DIGIKEY_CLIENT_ID=
DIGIKEY_CLIENT_SECRET=

MOUSER_API_KEY=
```

Copy these into your own `.env` (never commit real keys — `.env` is gitignored). See `.env.example` for the up-to-date list, including optional `DIGIKEY_BASE_URL`/`MOUSER_BASE_URL` overrides. A provider without credentials configured simply doesn't contribute any results — nothing else breaks, and no restart is needed for other features to keep working.

### Provider responsibilities

A provider adapter (`catalog/infrastructure/provider/<name>/`) only ever:

1. searches for products matching a query;
2. maps its own response shape to GridMind's common `CatalogResult` model;
3. optionally supplies candidate image URLs;
4. **never** creates an inventory item directly — search results are always a proposal the user reviews and confirms.

Provider-specific response types (e.g. `DigiKeyProduct`, `MouserPart`) never leave their own adapter package — `catalog.domain` and everything above it only ever sees `CatalogResult`.

### Provider selection

Two different kinds of source exist conceptually:

- **Electronic component distributors** (DigiKey, Mouser) — precise, MPN-driven, huge catalogs, built for sourcing parts at scale. This is what's implemented today.
- **Maker/dev-board catalogs** (Adafruit, SparkFun, generic Arduino/ESP32 module listings) — better suited to hobbyist boards that don't always have a distributor-style MPN. Not implemented yet; see the [Adding a new provider](#adding-a-new-provider) section below. Note that `CatalogResult` currently *requires* a non-blank MPN, so accommodating a maker source without one would need a small model change first.

### Search aggregation

```text
User search
    ↓
Configured providers (List<ProductCatalogProvider>)
    ↓
Each queried in parallel (virtual threads), isolated — one failing doesn't affect the others
    ↓
GridMind normalization (CatalogResult)
    ↓
Duplicate grouping (ProductGrouper — normalized manufacturer + MPN)
    ↓
Unified, cached search results
```

Grouping is deliberately simple and deterministic: results are merged only when their normalized manufacturer and MPN match — no fuzzy matching.

### Cache

Search results are cached in-memory (Caffeine, `shared/config/CacheConfig.kt`), keyed by the normalized query, for 24h — this avoids burning through a provider's daily quota on repeated searches. The cache is purely a performance detail: it's cleared on every backend restart and never backs anything persistent. An inventory item's data (including its stored image) has nothing to do with this cache — it lives in Postgres and on disk, independent of it.

### Images

```text
External provider
      ↓
Image candidate (URL, still external)
      ↓
User selection (confirmation step)
      ↓
Download (ImageStorageService)
      ↓
GridMind local storage (stored_images table + file on disk, served via /api/media/{id})
```

Once downloaded, an image no longer depends on the provider or on internet access — and if the exact same image is picked for a different item later, it's deduplicated by its SHA-256 checksum instead of being stored twice. A failed download never blocks item creation; the item is just saved without an image.

### Adding a new provider

1. Implement the `ProductCatalogProvider` interface (`catalog/infrastructure/provider/ProductCatalogProvider.kt`) in a new subpackage `catalog/infrastructure/provider/<name>/`.
2. Keep that provider's own request/response DTOs private to its own package — see `digikey/` or `mouser/` for the pattern.
3. Map its results to `CatalogResult`/`CatalogImage`; skip anything that can't be mapped (e.g. no MPN).
4. Register it as a Spring bean gated by `@ConditionalOnExpression` on its required config being non-empty, so the app keeps working with it unconfigured.
5. Add its config under `gridmind.catalog.<name>` in `application.yaml`, backed by env vars documented in `.env.example`.
6. Write a pure mapping test (no network) plus a live test gated by `@EnabledIfEnvironmentVariable`, so `./gradlew test` never needs real credentials to pass.
7. Update the table above.

## Notes

The backend is intentionally structured as a modular monolith with a clear split between domain, application, persistence, and HTTP layers per feature module. See `ROADMAP.md` for the planned direction (Gridfinity modeling, projects/BOM, AI-assisted import, and more).
