# GridMind Roadmap

GridMind is a smart inventory and workshop management application for makers.

The goal of the project is to build a clean, maintainable and interview-ready application while progressing step by step as a developer.

The project should evolve incrementally.

Do not implement future features before the foundations they depend on are stable.

---

# Phase 0 — Project foundations

## Goal

Have a clean project structure and a development environment that can be started easily.

## Backend

- [x] Kotlin + Spring Boot project starts correctly
- [x] Java 21 configured
- [x] Gradle Kotlin DSL working
- [x] PostgreSQL configured
- [x] Application configuration cleaned up
- [x] Package structure created
- [x] Global error handling prepared
- [x] Basic validation available

Initial backend structure:

```text
org.gridmind.backend
├── inventory
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
│       └── persistence
│
└── shared
    ├── config
    └── error
```

## Frontend

- [x] React + TypeScript + Vite working
- [x] Tailwind + daisyUI installed
- [x] Oxlint configured
- [x] Basic application layout created
- [x] Frontend can call the backend

Initial frontend structure:

```text
src/
├── app/
├── features/
│   └── inventory/
│       ├── api/
│       ├── components/
│       ├── pages/
│       └── types/
├── components/
│   └── common/
├── layouts/
└── theme/
```

## Done when

The backend and frontend both start locally and can communicate with each other.

---

# Phase 1 — Inventory MVP

## Goal

Create the first complete vertical feature of GridMind.

The user must be able to create and manage inventory items.

## Domain

Start with a deliberately small `Item`.

Initial fields:

```text
id
name
quantity
```

Possible additions later:

```text
description
manufacturer
reference
category
tags
notes
productUrl
datasheetUrl
minimumQuantity
```

Do not add every future field immediately.

## Backend

- [x] Create the `Item` domain model
- [x] Define its business rules
- [x] Create persistence representation
- [x] Create repository
- [x] Create application service
- [x] Create request/response DTOs
- [x] Create REST controller
- [x] Add validation
- [x] Add error handling
- [x] Add tests

Initial API (implemented under `/api/inventory` rather than `/api/items` — the sketch below is aspirational, kept for reference):

```http
GET    /api/items
GET    /api/items/{id}
POST   /api/items
PATCH  /api/items/{id}
DELETE /api/items/{id}
```

## Frontend

- [x] Create `Item` TypeScript type
- [x] Create inventory API client
- [x] Create inventory page
- [x] Display items
- [x] Create an item
- [x] Edit an item
- [x] Delete an item
- [x] Display loading state
- [x] Display empty state
- [x] Display error state
- [x] Display user feedback (daisyUI toast, not MUI Snackbar)

## Tests to add

- [x] Item cannot have a negative quantity
- [x] Item can be created
- [x] Item can be retrieved
- [x] Item can be updated
- [x] Unknown item returns a proper error
- [x] Item deletion works

## Done when

A user can add an item from React, save it in PostgreSQL, reload the page and still see it.

---

# Phase 2 — Categories and richer inventory

## Goal

Make inventory useful for real workshop hardware.

## Features

- [x] Categories
- [x] Manufacturer
- [x] Product reference
- [x] Description
- [x] Tags
- [x] Notes
- [x] Product URL
- [x] Datasheet URL
- [x] Minimum stock quantity

## Categories

Categories must be data-driven.

Examples:

```text
Microcontrollers
Electronic components
Sensors
Modules
Displays
Connectors
Power supplies
Mechanical parts
Fasteners
3D printing
Tools
Other
```

Do not hardcode categories in an enum if users must be able to create their own.

## Frontend

- [x] Search inventory
- [x] Filter by category
- [x] Filter by manufacturer
- [x] Display tags (daisyUI badge, not MUI Chip)
- [x] Item details page
- [x] Better create/edit form

## Done when

GridMind can realistically replace a basic spreadsheet for hardware inventory.

---

# Phase 3 — Storage management

## Goal

Know where every item is physically stored.

## Domain

Introduce hierarchical storage locations.

Example:

```text
Workshop
└── Electronics cabinet
    ├── Drawer 1
    │   ├── Bin A
    │   └── Bin B
    └── Drawer 2
```

## Backend

- [x] Create `StorageLocation`
- [x] Support parent/child relationships
- [x] Create storage API
- [x] Create stock allocation model
- [x] Allow one item to exist in several locations

Do not put a single `locationId` directly on Item.

Use a model similar to:

```text
Item
+
StorageLocation
+
Quantity
```

## Frontend

- [x] Storage hierarchy page
- [x] Browse location children
- [x] Display location contents
- [x] Display item locations
- [x] Move stock between locations

## Business rules

- [x] Quantity stored across locations cannot exceed available stock
- [x] Moving stock must be atomic
- [x] Invalid locations must be rejected

## Done when

A user can answer:

> Where are my ESP32s?

and GridMind can provide the physical location and quantity.

---

# Phase 4 — Gridfinity domain

## Goal

Model Gridfinity drawers and bins.

This phase should contain meaningful domain logic and strong unit tests.

## Drawer model

A Gridfinity-compatible drawer defines:

```text
width
depth
```

Example:

```text
6 × 4
```

## Bin model

A bin defines:

```text
x
y
widthUnits
depthUnits
heightUnits
```

Example:

```text
2 × 1 × 3U
```

## Backend rules

- [ ] Bin must remain inside drawer boundaries
- [ ] Bins cannot overlap
- [ ] Calculate total cells
- [ ] Calculate occupied cells
- [ ] Calculate available cells
- [ ] Calculate occupancy percentage
- [ ] Find whether a requested bin can fit

## Tests

This phase should have strong unit tests.

Examples:

```text
accepts bin in free area
rejects overlapping bin
rejects bin outside drawer
accepts bin exactly on boundary
calculates occupied cells
calculates free cells
```

## Done when

The Gridfinity domain can work and be tested without the frontend or database.

---

# Phase 5 — Gridfinity visualization

## Goal

Visually display the physical organization of a drawer.

## Frontend

Use CSS Grid for the physical Gridfinity representation.

MUI remains responsible for the surrounding interface.

Example:

```text
┌────┬────┬─────────┐
│ R1 │ R2 │ ESP32   │
├────┼────┤         │
│ R3 │ R4 │         │
├─────────┼─────────┤
│ JST     │ FREE    │
└─────────┴─────────┘
```

## Features

- [ ] Display drawer dimensions
- [ ] Display bins
- [ ] Respect real bin footprints
- [ ] Display free cells
- [ ] Display occupancy
- [ ] Select a bin
- [ ] Show bin contents

Possible later feature:

- [ ] Drag and drop bins

Do not implement drag and drop until basic placement works correctly.

## Done when

A user can visually understand how a Gridfinity drawer is organized.

---

# Phase 6 — Automatic Gridfinity placement

## Goal

Help the user find space for a new bin.

## Backend

Implement a placement algorithm.

Input:

```text
drawer
existing bins
requested bin size
```

Output:

```text
valid position
```

or:

```text
no available placement
```

Start with a simple deterministic scan.

Do not create an unnecessarily sophisticated spatial algorithm unless measurements show it is needed.

## Features

- [ ] Find first available position
- [ ] Detect no-fit condition
- [ ] Suggest compatible drawers
- [ ] Unit test different placement scenarios

## Done when

GridMind can answer:

> Where can I put a 2×2 bin?

---

# Phase 7 — Projects and BOM

## Goal

Connect inventory with maker projects.

## Project model

A project contains:

```text
name
description
status
required items
```

Example:

```text
HUB75 Clock

1 × ESP32-S3
1 × HUB75 64×32
1 × 5V power supply
4 × M3 screws
```

## Features

- [ ] Create project
- [ ] Create BOM
- [ ] Compare BOM with inventory
- [ ] Display missing items
- [ ] Calculate maximum buildable quantity

Example:

```text
ESP32-S3        2 available / 1 required
HUB75           1 available / 1 required
Power supply    0 available / 1 required
```

## Tests

- [ ] Enough stock
- [ ] Missing stock
- [ ] Multiple project quantities
- [ ] Zero quantity edge cases

## Done when

GridMind can determine whether a project can currently be built.

---

# Phase 8 — Product import foundation

## Goal

Prepare automatic product information extraction without coupling the domain to an AI provider.

## Architecture

Define a provider abstraction.

Conceptually:

```text
ProductInformationProvider
```

Possible future implementations:

```text
OpenAiProductInformationProvider
ThingiverseProvider
PrintablesProvider
MakerWorldProvider
```

## Product information

Possible extracted data:

```text
name
manufacturer
reference
description
category
technical specifications
tags
quantity
source URL
confidence
```

## Important rule

Imported information is always a proposal.

Workflow:

```text
Source
↓
Extraction
↓
Review
↓
User edits if necessary
↓
Confirmation
↓
Inventory
```

Never silently create inventory items from AI-generated information.

## Done when

The application can accept a structured product proposal independently of how it was extracted.

---

# Phase 9 — AI-assisted product import

## Goal

Use AI to analyze product information.

Possible inputs:

- [ ] Product description
- [ ] Product URL
- [ ] Image
- [ ] PDF
- [ ] Datasheet

## Features

- [ ] Extract structured product information
- [ ] Display confidence
- [ ] Allow editing
- [ ] Detect probable duplicates
- [ ] Confirm before creating item

## Duplicate detection

Example:

```text
This product appears to match:

ESP32-S3 DevKitC-1 N16R8
Current quantity: 2

Add 2 units to this existing item?
```

## Done when

Adding new workshop hardware becomes significantly faster without sacrificing user control.

---

# Phase 10 — 3D model integrations

## Goal

Associate inventory and storage with printable models.

Potential sources:

- [ ] Thingiverse
- [ ] Printables
- [ ] MakerWorld
- [ ] Generic URLs

## Architecture

Use independent providers.

Conceptually:

```text
ModelProvider
├── ThingiverseProvider
├── PrintablesProvider
├── MakerWorldProvider
└── GenericModelProvider
```

Do not make core GridMind features depend on third-party APIs being available.

## Features

- [ ] Associate model URL with a bin
- [ ] Display source
- [ ] Display image/preview if available
- [ ] Store model metadata
- [ ] Search supported sources where possible

## Done when

A Gridfinity bin can be linked to the model used to print it.

---

# Phase 11 — Gridfinity generation

## Goal

Help generate custom Gridfinity bins.

Possible approach:

```text
GridMind
↓
parameters
↓
OpenSCAD generator
↓
STL
```

## Parameters

Examples:

```text
width
depth
height
label
dividers
magnets
screw holes
```

## Features

- [ ] Define bin parameters
- [ ] Generate model
- [ ] Associate generated model with storage bin
- [ ] Keep generator integration isolated from domain logic

## Done when

GridMind can propose and generate a suitable bin for an inventory item.

---

# Phase 12 — Device management

## Goal

Prepare physical workshop integrations.

## Devices

Introduce:

```text
LED controller
ESP32 controller
drawer controller
```

A physical storage location may reference:

```text
controllerId
ledIndex
```

Do not mix physical device configuration with core inventory logic.

## Done when

GridMind can model physical controllers without yet requiring LEDs to work.

---

# Phase 13 — MQTT and drawer LEDs

## Goal

Locate physical items using LEDs.

Architecture:

```text
GridMind Backend
↓
MQTT
↓
ESP32
↓
WS2812B / LEDs
↓
Physical drawer/bin
```

## Features

- [ ] MQTT connection
- [ ] Controller registration
- [ ] Location-to-LED mapping
- [ ] Locate command
- [ ] LED timeout
- [ ] Device availability status

Example:

```text
Search: ESP32-S3
↓
GridMind finds location
↓
Locate
↓
LED flashes on drawer
```

## Done when

Selecting an item in GridMind can physically indicate where it is stored.

---

# Phase 14 — Search

## Goal

Make GridMind fast to use in a real workshop.

Search across:

- [ ] Item name
- [ ] Manufacturer
- [ ] Reference
- [ ] Category
- [ ] Tags
- [ ] Storage location
- [ ] Project

Possible later feature:

- [ ] Command palette

## Done when

Finding an item takes only a few seconds.

---

# Phase 15 — Authentication and users

## Goal

Add proper security only when the application actually needs it.

Possible features:

- [ ] User accounts
- [ ] Authentication
- [ ] User preferences
- [ ] Ownership
- [ ] Roles if genuinely necessary

Do not introduce OAuth2, Keycloak or complex identity infrastructure prematurely.

## Done when

GridMind can safely be exposed outside a trusted local network.

---

# Phase 16 — Production readiness

## Goal

Make the project easy to run and present.

## Backend

- [ ] Database migrations
- [ ] Production configuration
- [ ] Health checks
- [ ] Logging
- [ ] Security review

## Infrastructure

- [ ] Dockerfile backend
- [ ] Dockerfile frontend
- [ ] Docker Compose
- [ ] PostgreSQL container
- [ ] Optional MQTT broker
- [ ] Environment configuration

## CI

GitHub Actions should at least:

- [ ] Compile backend
- [ ] Run backend tests
- [ ] Build frontend
- [ ] Run frontend checks
- [ ] Run Oxlint

## Done when

A new developer can clone the repository and start GridMind with clear documented steps.

---

# Phase 17 — Documentation and interview preparation

## Goal

Make the engineering decisions visible.

Create:

```text
README.md
docs/
├── architecture.md
├── database.md
├── gridfinity.md
└── decisions/
```

Document important decisions such as:

- why Kotlin
- why Spring Boot
- why PostgreSQL
- why React
- why a modular monolith
- why microservices were not used
- domain/persistence separation
- Gridfinity placement algorithm
- MQTT architecture
- AI validation strategy

Consider lightweight Architecture Decision Records.

Example:

```text
docs/decisions/
001-modular-monolith.md
002-postgresql.md
003-react-spa.md
```

## Done when

You can explain the architecture of GridMind without needing to open every source file.

---

# Suggested implementation order

The recommended order is:

```text
1. Project foundations
2. Inventory MVP
3. Rich inventory
4. Storage
5. Gridfinity domain
6. Gridfinity visualization
7. Automatic placement
8. Projects / BOM
9. Product import abstraction
10. AI import
11. 3D model integrations
12. Gridfinity generator
13. Devices
14. MQTT / LEDs
15. Search improvements
16. Authentication
17. Production readiness
18. Documentation
```

---

# Development principles

Throughout the project:

- keep functions short and focused;
- keep cognitive complexity low;
- prefer clear names;
- avoid unnecessary abstractions;
- write meaningful tests;
- do not over-engineer;
- refactor when complexity appears;
- keep business logic independent from UI;
- keep external APIs isolated;
- keep controllers thin;
- use PostgreSQL constraints when appropriate;
- use migrations for database evolution;
- keep API contracts explicit;
- document important architectural decisions.

Most importantly:

> Build one complete and understandable feature at a time.

GridMind should grow through working vertical slices, not through large amounts of speculative architecture.