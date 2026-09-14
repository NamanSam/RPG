# CodeQuest RPG

A Java-learning adventure across a single vertically scrollable pixel-art world. Built independently in `D:\rpg game`; no StylePin files or services are required.

## Milestone 1

Implemented:

- Five original pixel environments, connecting paths, NPC dialogues, idle animations, moving water, quest markers, and locked-zone previews.
- Responsive public map preview and protected signed-in map.
- Register, login, logout, restore session, BCrypt passwords, signed HttpOnly JWT cookies, and CSRF protection.
- MySQL migrations for accounts, initial XP, and the five worlds.
- Backend integration tests and a real-HTTP authentication smoke test.

Not implemented yet: playable quests, grading, XP awards, badges, progress updates, and functional area unlocks. The map currently uses preview geography and preview locks. No pretend completions or localStorage rewards are used.

The planned content is **12 quests**, distributed 3 / 3 / 2 / 2 / 2 across the five worlds. See [milestones](docs/milestones.md) for the Day 4 end-to-end target.

## Run on this computer

Node and Java are installed. A project-local Maven distribution, independent MySQL data directory, and generated `.env` credentials have been prepared. Do not overwrite `.env`: it matches the initialized CodeQuest database. No Windows service or system configuration was changed.

If the development processes from this milestone are still running, simply visit **http://127.0.0.1:5173/**. The UI opens a map preview; create your own account using Sign in → Create an account.

To restart, use three PowerShell terminals. Run the following from `D:\rpg game` (PowerShell 7):

Terminal 1 — dedicated MySQL, port 3307:

```powershell
cd 'D:\rpg game'
.\scripts\mysql-local.ps1
```

Terminal 2 — Spring Boot API, port 8085:

```powershell
cd 'D:\rpg game'
.\scripts\backend.ps1 run
```

Terminal 3 — React, port 5173:

```powershell
cd 'D:\rpg game\frontend'
npm.cmd ci
npm.cmd run dev -- --strictPort
```

If dependencies are already installed, skip `npm.cmd ci`. Keep the terminals open. Stop these processes with Ctrl+C in their own terminals. Do not stop the machine's existing MySQL service. If 3307, 8085, or 5173 is already occupied by CodeQuest, reuse the running instance instead of launching a duplicate.

The preview at `/preview` works with just the frontend. Authentication requires both the API and database. The Vite dev server proxies `/api` to port 8085; use the frontend URL rather than opening the API as a website. API health: http://127.0.0.1:8085/api/health.

## Fresh installation / another computer

Prerequisites: Node 22.12+ (Node 24 used here), Java JDK 21+ (JDK 26 used here), Maven 3.9+, and MySQL. The project compiles to Java 21. Docker is an optional way to run an isolated MySQL 8.4 instance:

1. Copy `.env.example` to `.env` and replace all placeholder secrets with fresh random values.
2. Run `docker compose up -d db`. The database is bound to localhost port 3307 with a CodeQuest-only volume.
3. If Maven is missing, run `.\scripts\setup-maven.ps1`; it downloads Maven from Maven Central and verifies the SHA-512 checksum. It does not change PATH.
4. Run the backend and frontend commands above. `scripts/backend.ps1` loads the root `.env`; Spring Boot does not load this file by itself.

Use either Docker MySQL or the project-local MySQL process, not both on port 3307. The local helper assumes the data directory already initialized on this computer and does not touch another database installation's configuration.

For an independently provisioned MySQL server, create a dedicated `codequest` database and restricted database user, then set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Never use another project's database credentials. Flyway applies the schema at startup.

## Checks

```powershell
# Unit/integration tests: isolated H2 in MySQL compatibility mode, no MySQL required.
.\scripts\backend.ps1 test

# Real HTTP + MySQL integration (requires the API running).
.\scripts\smoke-auth.ps1

# Production frontend build.
cd frontend
npm.cmd run build
```

The smoke script creates a synthetic Smoke Explorer account per run. Browser QA also created a synthetic Map Explorer account; these are development-only records.

## Authentication notes

- JWT expires after two hours; re-login after expiry. No refresh-token subsystem in this milestone.
- Passwords are 8–64 characters and at most 72 UTF-8 bytes for BCrypt.
- CSRF tokens are obtained from `/api/auth/csrf`; fetch a fresh token after login/registration. The frontend handles this automatically.
- Logout clears the browser cookie. It does not revoke a copied JWT before its expiry.
- Local HTTP uses `COOKIE_SECURE=false`. Production HTTPS must use `true`, a strong new JWT secret, and frontend/API hosting under the same origin.
- Backend auth endpoints return player DTOs, never password hashes.

## Source layout

```text
frontend/src/
  App.jsx                     Routing and session bootstrap
  pages/                      Map and authentication pages
  features/world-map/         Geography, pixel scenery, NPC dialogue
  services/api.js             Cookie/CSRF-aware API client
  styles.css                  Responsive pixel-world styling
backend/src/main/java/com/codequest/
  auth/                       JWT, cookie filter, auth service/controllers
  user/                       Player entity and repository
  progress/                   Initial persistent XP
  config/                     Spring Security configuration
  common/                     Health and error responses
backend/src/main/resources/db/migration/
scripts/                      Local run and verification helpers
docs/                         Milestones and verification results
```

Pixel scenery is original, code-native SVG geometry. Typography requests DM Sans and Space Mono from Google Fonts, with local sans-serif/monospace fallbacks. No external game sprites are required.

## Technology references

Runtime requirements were checked against the official [Vite guide](https://vite.dev/guide/) and [Spring Boot requirements](https://docs.spring.io/spring-boot/4.0/system-requirements.html). Exact frontend dependencies are pinned in `package-lock.json`; backend dependencies are managed by the Spring Boot parent in `pom.xml`.
