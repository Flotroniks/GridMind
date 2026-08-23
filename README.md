# GridMind

GridMind is a smart inventory and workshop management application for makers — track hardware, organize where it's physically stored, and (eventually) plan projects around it.

## Stack

- **Backend**: Kotlin, Spring Boot, PostgreSQL, Flyway migrations
- **Frontend**: React, TypeScript, Vite, Material UI (MUI), TanStack Query, react-i18next
- **Local AI**: [Ollama](https://ollama.com) (CPU by default, no GPU required — optional GPU acceleration available) for the experimental image-analysis prototype — see [Local AI / Image analysis](#local-ai--image-analysis)
- **Dev environment**: Docker Compose (Postgres, backend, frontend, Ollama, all with live reload)

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

To try the experimental image-analysis prototype, also pull the vision model once Ollama is up (see [Local AI / Image analysis](#local-ai--image-analysis) for details):
```bash
docker compose exec ollama ollama pull qwen2.5vl:3b
```

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
- **Image analysis (experimental)**: photograph a part and get a best-effort identification from a local vision model, either as a standalone evaluation or as an alternative to manual entry when adding an item (pre-fills the form, photo becomes the item's image) — see [Local AI / Image analysis](#local-ai--image-analysis)

## Backend API

- `GET/POST /api/inventory/items`, `GET/PATCH/DELETE /api/inventory/items/{id}` — inventory items (`search`, `categoryId`, `manufacturer` query filters)
- `POST /api/inventory/items/with-photo` — create an item with a locally uploaded photo as its image (multipart: `item` JSON part + optional `image` file part), used by the image-analysis "add from a photo" flow
- `GET/POST /api/categories` — categories
- `GET/POST/DELETE /api/storage/locations`, `GET /api/storage/locations/{id}/contents` — storage hierarchy
- `GET /api/storage/items/{itemId}/stock`, `POST /api/storage/stock/allocate`, `POST /api/storage/stock/move` — stock allocation and movement
- `GET /api/catalog/search?query=` — search configured product catalog providers, grouped and normalized (see below)
- `GET /api/media/{id}` — serves a locally stored image by id
- `POST /api/image-analysis/analyze` — multipart image upload, analyzed locally via Ollama (see [Local AI / Image analysis](#local-ai--image-analysis))

## Project structure

```text
backend/src/main/kotlin/org/gridmind/backend/
├── inventory/       # items: domain, application, api, persistence (incl. local image storage)
├── category/        # categories
├── storage/         # storage locations and stock allocation
├── catalog/         # external product search: domain, application, api, provider adapters
├── imageanalysis/    # local image analysis prototype: domain, application, api, Ollama adapter
└── shared/           # cross-cutting config and error handling

backend/src/main/resources/db/migration/   # Flyway migrations

frontend/src/
├── app/                 # router, root component
├── features/
│   ├── inventory/
│   ├── categories/
│   ├── storage/
│   ├── catalog/         # product search UI (search step + confirmation step)
│   └── imageanalysis/   # image-analysis prototype UI
├── components/common/   # shared UI (toasts, theme toggle, language switcher, error boundary)
├── layouts/
└── theme/               # MUI theme + light/dark color mode
```

## Product catalog providers

When adding an inventory item, GridMind can look it up across external parts catalogs instead of requiring everything to be typed in by hand — search a name or MPN, pick the right result, review/edit before saving. Each provider is an independent, optional integration: GridMind works with zero, one, or several configured at once.

Picking a result opens a normal, editable item form pre-filled from it — including the category, matched by name against your existing GridMind categories when one of them matches the provider's suggested category (a one-click "Créer et utiliser" lets you create-and-select it otherwise). Submitting that form doesn't create the item immediately: it shows a short review — quantity, category, name, image — to confirm before actually saving, since a search result is a starting point to double-check, not something to commit blindly.

| Provider | Type | Status | Authentication | Images | Best for |
| --- | --- | --- | --- | --- | --- |
| [DigiKey](https://developer.digikey.com) | Electronic components | **Supported** | OAuth2 (client credentials) | Yes | General electronic components with a real MPN |
| [Mouser](https://www.mouser.com/api-hub/) | Electronic components | **Supported**¹ | API key | Yes | Second source, used to validate cross-provider grouping |
| [Adafruit](https://www.adafruit.com/products_api) | Maker hardware | **Supported** | None (public API) | Yes | Dev boards, breakout boards and modules that don't have a real distributor MPN |
| [eBay](https://developer.ebay.com/) | General marketplace | **Supported**² | OAuth2 (client credentials) | Yes | Anything not carried by a distributor — used/surplus parts, discontinued boards, hobbyist listings |
| [Octopart / Nexar](https://nexar.com/api) | Electronic components aggregator | **Rejected** | OAuth2 | Yes | — its terms cap caching at 24h and forbid self-hosting images, which conflicts directly with this project's "keep images locally, indefinitely" goal |
| [partsdb.io](https://www.partsdb.io/) | Electronic components (reichelt/Conrad, EU-focused) | **Planned** | API key | Not confirmed in its docs | Low-friction fallback (plain key, no OAuth, permissive terms) if DigiKey/Mouser coverage is ever insufficient |
| [SparkFun](https://www.sparkfun.com/) | Maker hardware | **Rejected** | — | — | No public product-catalog API exists (only hookup-guide/GitHub docs for their designs) — the only way to pull their catalog would be scraping the site, which is fragile and outside what this project is willing to depend on |

¹ Code is implemented and its mapping is validated against Mouser's real API schema, but Mouser reviews every API key request manually (1-2 business days) — a freshly issued key won't return results until it's approved on Mouser's side. Nothing on the GridMind side needs to change once that happens.

² Code is implemented and its request/response mapping is built directly from eBay's published OpenAPI schema for the Browse API — but unlike Mouser, no eBay developer credentials exist yet at all, so this one has never been exercised against the real, live API. Provide `EBAY_CLIENT_ID`/`EBAY_CLIENT_SECRET` (see below) to turn it on and confirm it end to end; the live test (`EbayApiClientLiveTest`) is ready and waiting for that.

Each supported provider in detail:

**DigiKey** — Product Information API v4, official API. Requires `DIGIKEY_CLIENT_ID` and `DIGIKEY_CLIENT_SECRET` (get them at [developer.digikey.com](https://developer.digikey.com): create an app, select the "Product Information V4" product). Free tier: 1000 requests/day. Returns name, manufacturer, MPN, description, category, datasheet URL, and an image. Note: an app's OAuth credentials are tied to the environment it was created for — a "Production" app's credentials are rejected by the sandbox host and vice versa; `DIGIKEY_BASE_URL` (default: production) lets you switch if needed.

**Mouser** — Search API v1, official API. Requires `MOUSER_API_KEY` (apply at [mouser.com/api-hub](https://www.mouser.com/en/api-hub/), Search API section — manual approval, 1-2 business days). Free tier: 1000 requests/day, 30/minute. Same data shape as DigiKey (name, manufacturer, MPN, description, category, datasheet, image).

**Adafruit** — [Products API](https://www.adafruit.com/products_api), public, no key required. Structurally different from DigiKey/Mouser: it has no keyword-search endpoint, only a full-catalog dump (`/api/products`, ~5,500 products, ~8MB). `AdafruitApiClient` fetches that once and keeps it in memory for 12h, refetching when it goes stale; `AdafruitProductCatalogProvider` filters the cached list by name/MPN/model and caps results at 20. Returns name, manufacturer (defaults to "Adafruit" when the feed leaves it blank), Adafruit's own store SKU as the MPN (e.g. `ADA5800` — a stable per-product reference, not a manufacturer-issued MPN), category, and an image. No description and no datasheet URL: the feed has neither. Category names are resolved lazily per result via Adafruit's single-category endpoint (`/api/category/{id}`, small) rather than its full category list (`/api/categories`, ~28MB with every product embedded per category) — resolved names are cached indefinitely per category ID, and a failed lookup is simply omitted rather than blocking or failing the search. Discontinued/pending and virtual (non-physical) listings are filtered out before matching. On by default (no credentials to gate on); set `ADAFRUIT_CATALOG_ENABLED=false` to turn it off, e.g. to skip fetching/caching its feed in a constrained environment.

**eBay** — [Buy Browse API](https://developer.ebay.com/api-docs/buy/browse/overview.html), official API, keyword search (`item_summary/search`). Requires `EBAY_CLIENT_ID` and `EBAY_CLIENT_SECRET` (get them at [developer.ebay.com](https://developer.ebay.com): sign in → My Account → Application Keys → create a keyset — use the **production** keyset, not sandbox, since sandbox only returns fake test listings). OAuth2 client-credentials, same shape as DigiKey but Basic-auth'd (`Authorization: Basic base64(clientId:clientSecret)`) rather than form-encoded, with scope `https://api.ebay.com/oauth/api_scope`. `EBAY_MARKETPLACE_ID` (default `EBAY_US`) selects which eBay marketplace is searched (currency, language, listings) — see `.env.example` for other values (`EBAY_GB`, `EBAY_DE`, `EBAY_FR`, ...).<br><br>eBay is a different kind of source from the others: it's a general marketplace, not a component distributor or a manufacturer's own catalog, so its results are marketplace listings, not canonical parts. Its search response carries no manufacturer/MPN field at all (only available, unreliably, via a separate per-item call this adapter doesn't make); `CatalogResult.manufacturer`/`mpn` being optional at the model level is what makes wiring this up possible without distorting anything — an eBay result is always its own, unmerged entry (see [Provider selection](#provider-selection)), never guessed into a distributor's part. `description` is eBay's own `shortDescription` field when present, else a "condition — price currency" fallback (e.g. `"Used — 12.99 EUR"`); `category` is resolved from the response's category list matched against its `leafCategoryIds` (no documented ordering guarantee on the category array itself, so array position alone isn't trusted). Capped at 10 results per search.

### Provider configuration

```env
DIGIKEY_CLIENT_ID=
DIGIKEY_CLIENT_SECRET=

MOUSER_API_KEY=

# ADAFRUIT_CATALOG_ENABLED=false

EBAY_CLIENT_ID=
EBAY_CLIENT_SECRET=
```

Copy these into your own `.env` (never commit real keys — `.env` is gitignored). See `.env.example` for the up-to-date list, including optional `DIGIKEY_BASE_URL`/`MOUSER_BASE_URL`/`ADAFRUIT_BASE_URL`/`EBAY_BASE_URL`/`EBAY_MARKETPLACE_ID` overrides. A provider without credentials configured simply doesn't contribute any results — nothing else breaks, and no restart is needed for other features to keep working. Adafruit needs no credentials at all, so it's enabled by default.

### Provider responsibilities

A provider adapter (`catalog/infrastructure/provider/<name>/`) only ever:

1. searches for products matching a query;
2. maps its own response shape to GridMind's common `CatalogResult` model;
3. optionally supplies candidate image URLs;
4. **never** creates an inventory item directly — search results are always a proposal the user reviews and confirms.

Provider-specific response types (e.g. `DigiKeyProduct`, `MouserPart`) never leave their own adapter package — `catalog.domain` and everything above it only ever sees `CatalogResult`.

### Provider selection

Three different kinds of source exist conceptually:

- **Electronic component distributors** (DigiKey, Mouser) — precise, MPN-driven, huge catalogs, built for sourcing parts at scale.
- **Maker/dev-board catalogs** (Adafruit) — better suited to hobbyist boards that don't always have a distributor-style MPN. Adafruit is implemented; SparkFun was evaluated and rejected for lack of a public catalog API (see the table above).
- **General marketplaces** (eBay) — not a catalog of canonical parts at all, but useful for the long tail a distributor won't carry: discontinued boards, used/surplus parts, one-off hobbyist listings. Results are never treated as authoritative the way a distributor's are — no manufacturer/MPN is asserted, and each listing stays its own entry rather than being merged with anything.

See [Adding a new provider](#adding-a-new-provider) below for wiring up another one.

`CatalogResult.mpn` is optional (`String?`), precisely so the common model doesn't force maker hardware into a distributor shape it doesn't have: a Wemos D1 Mini, a NodeMCU, an ESP32-CAM breakout, or a generic sensor module rarely carries a real MPN, and GridMind shouldn't require one to be searchable or inventoriable. `ProductGrouper` treats a missing MPN as "no reliable merge key" rather than a wildcard — two MPN-less results are never merged into each other, even with the same name and manufacturer, and an MPN-less result never absorbs (or is absorbed by) one that does have an MPN. This keeps grouping deterministic without guessing.

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

1. Implement the `ProductCatalogProvider` port (`catalog/application/ProductCatalogProvider.kt`) in a new adapter subpackage `catalog/infrastructure/provider/<name>/`.
2. Keep that provider's own request/response DTOs private to its own package — see `digikey/` or `mouser/` for the pattern.
3. Map its results to `CatalogResult`/`CatalogImage`; skip anything that can't be mapped (e.g. no name). A missing MPN is fine — leave it `null` rather than inventing one.
4. Register it as a Spring bean gated on its config: `@ConditionalOnExpression` checking required credentials are non-empty for a key/OAuth-based API (see `digikey/`, `mouser/`), or `@ConditionalOnProperty` with `matchIfMissing = true` for a public API that's on by default (see `adafruit/`) — either way, the app keeps working whether or not it's active.
5. Add its config under `gridmind.catalog.<name>` in `application.yaml`, backed by env vars documented in `.env.example`.
6. Write a pure mapping test (no network) plus a live test gated by `@EnabledIfEnvironmentVariable`, so `./gradlew test` never needs real credentials — or network access — to pass, even for a provider that needs no credentials at all (see `AdafruitApiClientLiveTest`, gated on an opt-in `RUN_ADAFRUIT_LIVE_TEST` flag instead).
7. Update the table above.

### Architecture decisions

GridMind is a modular monolith, not a full hexagonal/clean-architecture rewrite: `domain`/`application`/`infrastructure`/`api` layering is applied per feature only where it earns its keep, and only external dependencies (catalog providers, image storage) sit behind an explicit port. No generic factories, no `Service`/`ServiceImpl` pairs, no interface for something with a single implementation and no foreseeable second one.

- **`ProductCatalogProvider` is a port in `catalog/application/`, not `catalog/infrastructure/`.** It's the seam the catalog feature depends on to reach the outside world (DigiKey, Mouser, Adafruit, the in-memory fake), so it belongs with the use case that depends on it (`ProductSearchService`), not with the adapters that implement it. This mirrors the existing `ImageDownloader` port in `inventory/application/` (implemented by `RestClientImageDownloader` in `inventory/infrastructure/media/`) — one consistent pattern for "external dependency behind a port," applied to both features rather than invented twice.
- **`CatalogResult.mpn` is `String?`, not `String`.** The initial model made MPN mandatory, which is true for DigiKey/Mouser-style distributor data but not for maker hardware (dev boards, breakout boards, common sensor modules), which often has no real MPN at all. Rather than inventing a placeholder value, the model allows `null` and `ProductGrouper` treats it as "no merge key" — see [Provider selection](#provider-selection).
- **Adafruit's adapter fetches and caches a full catalog instead of calling a search endpoint per query**, because Adafruit's Products API doesn't have one — it only exposes a full-list dump. Rather than distorting `ProductCatalogProvider`'s contract (still just `search(query): List<CatalogResult>`) to accommodate this, the difference is absorbed entirely inside `AdafruitApiClient` (fetch-and-cache) and `AdafruitProductCatalogProvider` (in-memory filter); nothing above the adapter needs to know Adafruit works differently from DigiKey or Mouser.

## Local AI / Image analysis

**Experimental.** Photograph a maker/electronics part, get a best-effort structured identification from a vision model running entirely locally via [Ollama](https://ollama.com) — no cloud API, no external service. Available two ways: as a standalone evaluation page (upload → analysis → result, nothing saved), and as an alternative to manual entry when adding an inventory item (analysis pre-fills the item form, and the photo becomes the item's image) — see [Usage from GridMind](#usage-from-gridmind) below. Still **not** wired into DigiKey/Mouser/Adafruit/eBay — it identifies an object from a photo, it doesn't look one up in a distributor catalog.

### Role of Ollama

Ollama runs as its own service in the Docker Compose stack (`ollama/ollama` image), reachable only from the backend over the internal Compose network at `http://ollama:11434` — it is **not** published to the host. The backend talks to it through one interface, `ImageAnalysisPort`; nothing above that interface knows Ollama exists (see [Architecture](#architecture-1) below). Models are pulled once into a persistent volume (`ollama_data`) so they survive a container restart without re-downloading.

### Model chosen: `qwen2.5vl:3b`

| Property | Value |
| --- | --- |
| Model | Qwen2.5-VL, 3B parameters, `q4_K_M` quantization (Ollama's default tag) |
| Download size | ~3.2GB |
| Context window | up to 125K tokens (far more than needed here) |

Chosen from what's currently available in Ollama's library of compact vision models, evaluated against this project's actual need — **reading markings, silkscreen text and part references off a photographed object is the dominant signal**, not general scene description:

- **`moondream` (1.8B)** — the smallest and fastest option, but explicitly weaker at detailed OCR/dense text reading than Qwen2.5-VL at a similar size; a poor fit for a feature whose whole point is reading PCB silkscreen and chip markings.
- **`llava-phi3` (3.8B)** — compact and CPU-friendly, but LLaVA-family models are trained on more general image-captioning data and are noticeably weaker on structured/text-heavy content (charts, screenshots, dense labels) than Qwen2.5-VL at the same size class.
- **`qwen2.5vl:3b` (3.75B, chosen)** — squarely in the requested 3-4B range, and Qwen2.5-VL's training deliberately emphasizes structured visual content and OCR, which is exactly this feature's use case. Best precision/speed trade-off for the stated need without going bigger.
- **`minicpm-v` (8B) / `qwen2.5vl:7b` / `llama3.2-vision` (11B)** — meaningfully stronger OCR/document understanding, but 2-3x the parameter count. On a 6-core/12-thread CPU with no GPU, that's a large latency cost for a "sometimes better" gain that isn't clearly justified for a prototype whose job is to evaluate whether the *concept* works at all. Worth revisiting later if `qwen2.5vl:3b`'s accuracy turns out to be the limiting factor, not the model class.

`qwen2.5vl:3b` was picked as the best compromise for this hardware and this exact task; see [Limitations](#limitations) below for how that plays out in practice.

### Resources — CPU-only by default, no GPU assumed

- The base `compose.yaml` uses no GPU configuration at all (no `deploy.reservations.devices`, no CUDA/ROCm image variant) — the `ollama/ollama` image runs CPU-only automatically when no GPU is passed through, so this works unmodified on a Ryzen 5 PRO 4650GE VM with no dedicated GPU. This is deliberate and stays the default: the deployment target has no *guaranteed* GPU, so the app must not require one.
- No hard memory/CPU limit is set on the `ollama` service — consistent with every other service in this stack, none of which are resource-constrained either. A loaded `qwen2.5vl:3b` uses on the order of a few GB of RAM (CPU) or VRAM (GPU) while active; with ~82GB available on the target host, this is not a meaningful pressure point for a single-user prototype used for occasional analyses.
- Ollama unloads a model from memory automatically 5 minutes after its last use (its default `keep_alive` behavior) — so RAM/VRAM usage for this feature is transient, not a standing reservation, even though nothing here configures that explicitly.
- **Recommended starting point for the target Ryzen 5 PRO 4650GE VM**: 4 vCPU / 8GB RAM dedicated to the container running Ollama is comfortable for `qwen2.5vl:3b` CPU inference; the full 6c/12t host has plenty of headroom beyond that for the rest of the stack.
- **Optional GPU acceleration** — if the machine running GridMind *does* have a dedicated NVIDIA GPU (a dev machine, most likely — not the CPU-only Proxmox target), `compose.gpu.yaml` layers a GPU reservation onto the `ollama` service without touching the base file:
  ```bash
  docker compose -f compose.yaml -f compose.gpu.yaml up -d
  ```
  Requires the NVIDIA driver and Docker's GPU support on the host (Docker Desktop's WSL2 backend already has this if `docker run --gpus all nvidia/cuda:12.4.1-base-ubuntu22.04 nvidia-smi` succeeds). Ollama detects and uses the GPU automatically inside the container — nothing else to configure, no code changes, same model. Not part of the default `up` command specifically so the CPU-only path — the one that has to work on the real target — stays what everyone gets without extra steps.

### Inference speed — CPU by default, dramatically faster with the optional GPU

There is no GPU acceleration unless `compose.gpu.yaml` is layered on (see above). Measured directly, real photos, real model, this exact Docker setup:

| | Cold (model load) | Warm |
| --- | --- | --- |
| CPU (dev-machine Ryzen, no GPU override) | ~1-2 min | **5-10 seconds** |
| GPU (dev-machine RTX 3070, `compose.gpu.yaml`) | ~1 min (one-time, loading weights into VRAM) | **under 1-2 seconds** |

Neither number is a guarantee — image complexity and host load both matter — but the relative gap (CPU warm analyses taking several seconds vs. GPU warm analyses completing in about a second) is real and reproducible, not a rounding error. The read timeout defaults to a generous 150s (`OLLAMA_TIMEOUT`, see below) specifically to give the CPU path headroom for slower cases; a GPU host has no trouble staying well under that. This is an accepted, deliberate CPU-first trade-off for a prototype used for occasional, one-off analyses on hardware that isn't guaranteed a GPU — GPU acceleration is a bonus where available, not a requirement anywhere in the code.

One specific failure mode is worth calling out because it was actually hit during testing: without a cap on output length, Ollama's schema-constrained decoding on this model occasionally fell into a repetition loop and never emitted the closing brace of the JSON — one test run generated 4,000+ tokens over several minutes before being cut off, instead of the well under 1,000 tokens the schema actually needs. `OllamaApiClient` now sets `num_predict` (a hard cap) and a raised `repeat_penalty` specifically to bound and reduce this — see [Limitations](#limitations) for what's still observed even with that mitigation in place.

### Docker setup

```bash
docker compose up -d
docker compose exec ollama ollama pull qwen2.5vl:3b
```

Add `-f compose.gpu.yaml` to the first command (see [Resources](#resources--cpu-only-by-default-no-gpu-assumed) above) on a machine with a dedicated NVIDIA GPU — the model only needs pulling once either way, since both paths share the same `ollama_data` volume.

The first command starts (among everything else) the `ollama` service; the second downloads the model into its persistent volume — a one-time step per volume (`docker compose down -v` would remove it and require re-pulling). There is no way to make Docker Compose pull an Ollama model on its own as part of `up`, so this second command is a required manual step, documented here exactly as run.

### Configuration

```env
OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_VISION_MODEL=qwen2.5vl:3b
OLLAMA_TIMEOUT=150s
```

All three are optional — the defaults above (matching `application.yaml`) are correct for the Docker Compose setup and need no `.env` entry unless you want to override them (e.g. pointing at a different model, or a longer timeout on slower hardware). None of these are hardcoded in application code — see `gridmind.imageanalysis.ollama.*` in `application.yaml` and `OllamaVisionAdapter`/`OllamaApiClient`.

### Usage from GridMind

**Standalone evaluation** — open the app → **Analyse IA** in the top navigation → select a JPEG or PNG photo of a part → **Analyser**. The structured result (type, probable name/model, manufacturer, visible text, characteristics, confidence, suggested search terms) is displayed — each field only appears if the model actually returned it. Nothing is saved here: closing or navigating away discards everything, and the backend never writes this upload to disk.

**Adding an item from a photo (guided)** — on the Inventaire page, **Ajouter un objet** → **Analyser une photo (IA)** (this and **Tout faire manuellement** are always shown, right below the search bar, not just when a text search finds nothing). The analysis result pre-fills the same item form a catalog search result would (name — falling back to the detected object type when the model didn't commit to a precise name — manufacturer, reference, a suggested category, visible text as notes), through the same review-before-confirm step. Here, unlike the standalone page, **the photo is uploaded and kept**: it becomes the item's image via `POST /api/inventory/items/with-photo`, stored the same way a catalog-provider image is (deduplicated by SHA-256 checksum, served from `/api/media/{id}`) — just uploaded directly instead of downloaded from a URL, since there's no provider URL for a local photo.

**Adding an item from a photo (manual form)** — on **Ajouter un objet** → **Tout faire manuellement**, the plain item-creation form itself has an optional photo picker at the top. Selecting a photo reveals a **Remplir avec IA** button: it runs the same local analysis and fills in whichever fields it found something for (name, manufacturer, reference, description, notes) without touching fields already filled in by hand, so it can be used before, mid-way through, or after typing. The photo becomes the item's image on submit exactly as in the guided flow above. Not available when editing an existing item — replacing an item's image isn't supported yet.

### Architecture

```text
Image analysis use case (ImageAnalysisService)
        ↓
ImageAnalysisPort
        ↑
OllamaVisionAdapter  →  OllamaApiClient  →  Ollama (/api/chat)
```

Same pragmatic-hexagonal pattern as `ProductCatalogProvider` (catalog) and `ImageDownloader` (inventory): the use case depends on a port it owns (`imageanalysis/application/ImageAnalysisPort.kt`), and the one adapter that implements it today (`imageanalysis/infrastructure/ollama/`) is the only code that knows Ollama exists. Response *shape* is enforced by Ollama itself via [structured outputs](https://docs.ollama.com/capabilities/structured-outputs) — a JSON schema passed in the `format` request field — so there's no "hope the model returned valid JSON" step; only the *content* of that JSON (parsing it and mapping it to `ImageAnalysisResult`) is this adapter's job.

Everything Ollama-specific — HTTP request shaping (`OllamaApiClient`), the system prompt (`VisionAnalysisPrompt.kt`), response parsing and error translation (`OllamaVisionAdapter`) — lives in `infrastructure/ollama/` and nowhere else. `ImageAnalysisController` only ever sees `ImageAnalysisService`/`ImageAnalysisResult`.

Wiring the analyzed photo into item creation reuses the existing image pipeline rather than inventing a second one: `ImageStorageService.storeUploaded(bytes, contentType, sourceProvider)` sits next to the existing `downloadAndStore(url, sourceProvider)`, sharing the same checksum-dedup/disk-write/`StoredImage` machinery — the only real difference is *where the bytes come from* (an uploaded `MultipartFile` vs. a downloaded URL), so `sourceUrl` is simply left `null` for an upload. A new endpoint, `POST /api/inventory/items/with-photo` (multipart: an `item` JSON part + an optional `image` file part), sits next to the existing JSON `POST /api/inventory/items` rather than replacing it — manual entry and catalog-search creation don't have a file to upload and keep using the plain JSON endpoint unchanged. `UploadedImageValidator` (real content-sniffing via `ImageIO`, not filename/Content-Type) moved from `imageanalysis/application/` to `shared/validation/` once it had two genuine callers (the image-analysis upload and this one) instead of one.

### Limitations

- **The standalone evaluation page never persists anything** (by design — see [Usage from GridMind](#usage-from-gridmind)); the item-creation path does persist the photo as the item's image.
- **Occasionally still fails to produce valid JSON, even with `num_predict`/`repeat_penalty` tuning.** Observed directly during testing: for some images, the model still generates unusually long, repetitive output and gets cut off by `num_predict` before completing the JSON object — the request succeeds at the HTTP level (Ollama responds 200) but the response can't be parsed, and GridMind surfaces this as a clean "response could not be parsed" error rather than a crash. This is a real, observed limitation of schema-constrained decoding on a 3B model, not a hypothetical — worth watching for during evaluation, and one of the concrete things a larger model might improve on if this turns out to be the limiting factor.
- **CPU inference is slow relative to a GPU** — see above for real measured numbers. Not suitable as-is for a bulk/batch workflow.
- **WEBP is not accepted.** Only JPEG and PNG: the JDK's built-in `ImageIO` has no WEBP reader, and adding a plugin just for this prototype wasn't judged worth it — see `UploadedImageValidator`.
- **A 3B model will sometimes misread dense or small text**, especially on low-resolution or poorly-lit photos. The prompt explicitly asks the model to prefer leaving a field empty over guessing, but this is a mitigation, not a guarantee — treat every result as a starting point to verify, never as ground truth.
- **No streaming, no partial results** — the frontend waits for the full response; there's no progressive "thinking" indicator beyond a generic loading state.
- **Single in-flight request model** — there's no queueing or concurrency control if multiple analyses are triggered at once; each is an independent HTTP call to Ollama, which will simply queue them internally.

## Notes

The backend is intentionally structured as a modular monolith with a clear split between domain, application, persistence, and HTTP layers per feature module. See `ROADMAP.md` for the planned direction (Gridfinity modeling, projects/BOM, AI-assisted import, and more).
