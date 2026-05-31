# Wevol-lite — Application Detail

Single source of truth for this project. Read this at the start of every working session before making any changes.

---

## 1. What This App Is

**Wevol-lite** is a private web app for one couple to save memories, upload photos, and plan trips together. It is a simplified version of the original Wevol project with authentication, notifications, invite flow, RabbitMQ, and Redis removed.

There is no login. Anyone with the URL can access the app. Keep the URL private.

---

## 2. What Was Removed Compared to Original Wevol

| Removed | Reason |
|---|---|
| Login / Register / Invite views | No auth |
| `auth-service` (Spring Boot) | No auth |
| `notification-service` (Spring Boot) | Simplified scope |
| `api-gateway` (Spring Cloud Gateway) | Single monolith has no need |
| RabbitMQ | Only used for notification events |
| Redis | Was a cache layer only |
| JWT tokens | No auth |
| Spring Security | No auth |
| NotificationBell component | No notifications |
| `notification.js` store | No notifications |
| Per-user identity | All actions attributed to fixed USER_ID |
| Multiple DB schemas | All tables in `public` schema |

---

## 3. Architecture

```
Browser
  │
  ├── Vercel (Vue 3 frontend)
  │     └── /api/* → proxied to Render backend
  │
  └── Render (Spring Boot monolith, port 8080)
        ├── /api/memories
        ├── /api/media + /api/albums
        ├── /api/trips
        └── /api/search
              │
              ├── Neon PostgreSQL (all tables in public schema)
              └── Cloudflare R2 (photo storage, presigned URLs)
```

**No message queue. No cache layer. No auth layer.**

---

## 4. Fixed Constants (AppConstants.java)

These two UUIDs are hardcoded. All data belongs to the couple identified by COUPLE_ID.

```
COUPLE_ID = 11111111-1111-1111-1111-111111111111
USER_ID   = 22222222-2222-2222-2222-222222222222
```

No auth headers are sent from the frontend. The backend always uses these constants.

---

## 5. Tech Stack

### Backend
- Java 17, Spring Boot 3.2.5
- Spring Data JPA + Hibernate
- Flyway migrations
- PostgreSQL 16 (Neon)
- AWS SDK v2 (S3 API for Cloudflare R2)
- No Spring Security, no RabbitMQ, no Redis

### Frontend
- Vue 3 (Composition API, `<script setup>`)
- Vite 5
- Pinia stores
- Vue Router 4
- Axios (no JWT interceptor)
- Day.js
- Lucide Vue icons
- VueUse
- Tailwind CSS v4
- vuedraggable (trip stop reordering)

---

## 6. Project File Structure

```
Wevol-lite/
├── deployment.md              ← deploy steps and env vars reference
├── applicationDetail.md       ← this file
├── docker-compose.yml         ← local dev (Postgres + backend)
├── .env.example               ← copy to .env, fill R2 credentials
├── .gitignore
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/wevol/
│       │   ├── WevolApplication.java
│       │   ├── config/
│       │   │   ├── AppConstants.java      ← COUPLE_ID and USER_ID constants
│       │   │   ├── CorsConfig.java        ← allows all origins
│       │   │   └── R2Config.java          ← S3Client, S3Presigner, CORS setup
│       │   ├── exception/
│       │   │   ├── AppException.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── memory/                    ← Memory CRUD
│       │   ├── media/                     ← Album CRUD + R2 presign/confirm
│       │   ├── trip/                      ← Trip + TripStop CRUD
│       │   └── search/                    ← Full-text search (PostgreSQL tsvector)
│       └── resources/
│           ├── application.yml
│           └── db/migration/
│               ├── V1__init_schema.sql    ← all tables
│               ├── V2__add_url_to_memory_media.sql
│               └── V3__add_search_vectors.sql  ← tsvector + triggers
└── frontend/
    ├── vercel.json                        ← SPA rewrite rule
    ├── vite.config.js                     ← proxy /api → backend
    ├── package.json
    └── src/
        ├── App.vue                        ← minimal, no auth bootstrap
        ├── main.js
        ├── api/axios.js                   ← plain axios, no JWT interceptor
        ├── router/index.js                ← no auth guards
        ├── stores/
        │   ├── auth.js                    ← stub: couple name/start_date only
        │   ├── memory.js
        │   ├── gallery.js
        │   ├── media.js
        │   ├── trip.js
        │   ├── search.js
        │   └── ui.js
        ├── components/
        │   ├── AppShell.vue               ← sidebar + mobile nav (no NotificationBell)
        │   ├── AddMemoryDialog.vue
        │   ├── AddAlbumDialog.vue
        │   ├── AddTripDialog.vue
        │   ├── AddStopDialog.vue
        │   ├── EditActionBar.vue
        │   ├── DeleteConfirmDialog.vue
        │   ├── DiscardConfirmDialog.vue
        │   ├── EmptyState.vue
        │   ├── MoodSelector.vue
        │   ├── SearchBar.vue
        │   └── BackButton.vue
        └── views/
            ├── DashboardView.vue          ← days together, stats, recent memories
            ├── TimelineView.vue           ← milestone timeline
            ├── MemoriesView.vue
            ├── MemoryDetailView.vue
            ├── GalleryView.vue            ← album list
            ├── GalleryFolderView.vue      ← photos inside album
            ├── TripsView.vue
            ├── TripDetailView.vue         ← itinerary + stops + budget
            └── ProfileView.vue            ← set couple name + start date
```

---

## 7. Database Schema (all in `public` schema)

### memories
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| couple_id | UUID | always COUPLE_ID constant |
| created_by | UUID | always USER_ID constant |
| title | VARCHAR(255) | required |
| note | TEXT | |
| memory_date | DATE | |
| location | VARCHAR(255) | |
| mood | VARCHAR(50) | happy / romantic / cozy / adventure |
| search_vector | tsvector | auto-updated by trigger |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### memory_media
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| memory_id | UUID FK → memories | cascade delete |
| media_id | UUID | references media.id |
| sort_order | INT | |
| url | VARCHAR(1000) | R2 public URL |

### media
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| couple_id | UUID | |
| uploaded_by | UUID | |
| r2_key | VARCHAR(500) | path in R2 bucket |
| url | VARCHAR(1000) | public URL |
| thumb_url | VARCHAR(1000) | |
| mime_type | VARCHAR(100) | |
| size_bytes | BIGINT | |
| uploaded_at | TIMESTAMP | |

### albums
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| couple_id | UUID | |
| title | VARCHAR(255) | |
| cover_media_id | UUID FK → media | nullable |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### album_photos
| Column | Type | Notes |
|---|---|---|
| album_id | UUID FK → albums | composite PK |
| media_id | UUID FK → media | composite PK |
| added_at | TIMESTAMP | |

### trips
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| couple_id | UUID | |
| created_by | UUID | |
| name | VARCHAR(255) | required |
| start_date | DATE | |
| end_date | DATE | |
| status | VARCHAR(50) | planning / confirmed / ongoing / done |
| notes | TEXT | |
| budget | DECIMAL(12,2) | |
| currency | VARCHAR(10) | default VND |
| search_vector | tsvector | auto-updated by trigger |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### trip_stops
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| trip_id | UUID FK → trips | cascade delete |
| day_number | INT | 1-based |
| time | VARCHAR(50) | e.g. "09:00" |
| place_name | VARCHAR(255) | required |
| place_type | VARCHAR(50) | hotel / flight / food / activity |
| address | VARCHAR(255) | |
| notes | TEXT | |
| cost | DECIMAL(10,2) | |
| currency | VARCHAR(10) | default VND |
| position | INT | for drag-and-drop ordering |
| created_at | TIMESTAMP | |

---

## 8. API Endpoints

All endpoints are public (no auth header required).

### Memories — `/api/memories`
| Method | Path | Description |
|---|---|---|
| GET | `/api/memories` | List all memories |
| GET | `/api/memories/{id}` | Get one memory |
| POST | `/api/memories` | Create memory |
| PUT | `/api/memories/{id}` | Update memory |
| DELETE | `/api/memories/{id}` | Delete memory |
| POST | `/api/memories/{id}/media` | Attach media to memory |

### Media — `/api/media`
| Method | Path | Description |
|---|---|---|
| POST | `/api/media/presign` | Get presigned R2 upload URL |
| POST | `/api/media/confirm` | Confirm upload, save media record |
| DELETE | `/api/media/{id}` | Delete media from R2 + DB |

### Albums — `/api/albums`
| Method | Path | Description |
|---|---|---|
| GET | `/api/albums` | List all albums |
| GET | `/api/albums/{id}` | Get album with photos |
| POST | `/api/albums` | Create album |
| PUT | `/api/albums/{id}` | Update album (title, cover) |
| DELETE | `/api/albums/{id}` | Delete album |
| POST | `/api/albums/{id}/photos` | Add photos to album |
| DELETE | `/api/albums/{id}/photos/{mediaId}` | Remove photo from album |

### Trips — `/api/trips`
| Method | Path | Description |
|---|---|---|
| GET | `/api/trips` | List all trips |
| GET | `/api/trips/{id}` | Get trip with stops |
| POST | `/api/trips` | Create trip |
| PUT | `/api/trips/{id}` | Update trip |
| DELETE | `/api/trips/{id}` | Delete trip |
| POST | `/api/trips/{tripId}/stops` | Add stop to trip |
| PUT | `/api/trips/{tripId}/stops/{stopId}` | Update stop |
| DELETE | `/api/trips/{tripId}/stops/{stopId}` | Delete stop |
| DELETE | `/api/trips/{tripId}/days/{dayNumber}` | Delete entire day |
| PUT | `/api/trips/{tripId}/stops/reorder` | Reorder stops (drag-drop) |

### Search — `/api/search`
| Method | Path | Description |
|---|---|---|
| GET | `/api/search?q=...&type=all` | Search memories and/or trips |

`type` values: `all`, `memories`, `trips`. Min query length: 2 chars. Uses PostgreSQL `tsvector` full-text search.

---

## 9. Frontend Auth Store (Stub)

`src/stores/auth.js` is a minimal stub — no API calls, no localStorage. It holds couple display info only.

**Available state/actions:**
```js
auth.couple          // { start_date, name, anniversary }
auth.initials        // always "We"
auth.coupleName      // couple.name or "Us"
auth.setStartDate(date)    // update start_date
auth.updateCouple(data)    // merge any couple fields
```

DashboardView and TimelineView use `auth.couple.start_date` for the days-together counter.
ProfileView lets the user set `couple.name` and `couple.start_date` locally (in-memory only, resets on page reload).

> **Known limitation:** Profile changes are not persisted to a database. If persistence is needed, add a `/api/couple` endpoint in the backend.

---

## 10. Photo Upload Flow

1. Frontend calls `POST /api/media/presign` with `{ filename, mimeType }`
2. Backend saves a Media record with empty URL, returns `{ mediaId, uploadUrl, r2Key }`
3. Frontend uploads file directly to R2 using the presigned PUT URL
4. Frontend calls `POST /api/media/confirm` with `{ mediaId, mimeType, sizeBytes }`
5. Backend sets `media.url = R2_PUBLIC_URL + "/" + r2Key`
6. For memories: frontend then calls `POST /api/memories/{id}/media` with `{ mediaId, url }`

---

## 11. Design System

Colors and styles match the original Wevol app (CSS variables in `src/assets/theme.css`):

- Background: soft white with gradient orbs (pastel pink top-right, pastel blue bottom-left)
- Sidebar: glassmorphic, 80px wide, floats on desktop
- Mobile: bottom navigation bar (5 tabs) + top header bar
- Primary gradient: `var(--gradient-primary)` (blue-to-pink)
- Cards: `var(--color-surface)` background, subtle border
- Font: system sans-serif

Nav tabs: Dashboard · Memories · Gallery · Trips · Timeline

---

## 12. Local Development

```bash
# Backend + DB
cp .env.example .env      # fill in R2 credentials
docker compose up -d      # starts Postgres on :5432, backend on :8080

# Frontend
cd frontend
npm install
npm run dev               # → http://localhost:5173
```

Vite dev proxy: all `/api/*` → `http://localhost:8080`

Backend reads `.env` via Docker Compose. Frontend reads `VITE_API_URL` from `.env.local` (create one if needed, default is empty which uses Vite proxy).

---

## 13. Implemented Features

| Feature | Key files | Notes |
|---|---|---|
| Dashboard | `DashboardView.vue`, `stores/memory.js`, `stores/trip.js` | Days counter, stats, recent 4 memories, upcoming trip |
| Milestone timeline | `TimelineView.vue` | 14 milestones from 100 days to 10 years |
| Memories CRUD | `MemoriesView.vue`, `MemoryDetailView.vue`, `memory/` backend | With photo attach, mood, location, date |
| Photo upload | `GalleryView.vue`, `GalleryFolderView.vue`, `media/` backend | Presigned R2 upload, album management |
| Trip planning | `TripsView.vue`, `TripDetailView.vue`, `trip/` backend | Day-by-day stops, budget tracker, drag-drop reorder |
| Full-text search | `SearchBar.vue`, `stores/search.js`, `search/` backend | Searches memories + trips via tsvector |
| Profile | `ProfileView.vue` | Set couple name and start date (in-memory only) |

---

## 14. Known Gaps / Future Work

- Profile/couple settings are in-memory only (not persisted). Add `GET/PUT /api/couple` endpoint if needed.
- No real-time sync between partners (both must refresh to see each other's changes).
- Search requires at least 2 characters.
- `created_by` / `uploaded_by` columns exist in DB but are unused in UI (always USER_ID).
