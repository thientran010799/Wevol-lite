CREATE TABLE IF NOT EXISTS memories (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    couple_id    UUID NOT NULL,
    created_by   UUID NOT NULL,
    title        VARCHAR(255) NOT NULL,
    note         TEXT,
    memory_date  DATE,
    location     VARCHAR(255),
    mood         VARCHAR(50),
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS memory_media (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    memory_id   UUID NOT NULL REFERENCES memories(id) ON DELETE CASCADE,
    media_id    UUID NOT NULL,
    sort_order  INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS media (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    couple_id    UUID NOT NULL,
    uploaded_by  UUID NOT NULL,
    r2_key       VARCHAR(500) NOT NULL,
    url          VARCHAR(1000) NOT NULL,
    thumb_url    VARCHAR(1000),
    mime_type    VARCHAR(100),
    size_bytes   BIGINT,
    uploaded_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS albums (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    couple_id      UUID NOT NULL,
    title          VARCHAR(255) NOT NULL,
    cover_media_id UUID REFERENCES media(id) ON DELETE SET NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS album_photos (
    album_id   UUID NOT NULL REFERENCES albums(id) ON DELETE CASCADE,
    media_id   UUID NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    added_at   TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (album_id, media_id)
);

CREATE TABLE IF NOT EXISTS trips (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    couple_id   UUID NOT NULL,
    created_by  UUID NOT NULL,
    name        VARCHAR(255) NOT NULL,
    start_date  DATE,
    end_date    DATE,
    status      VARCHAR(50) NOT NULL DEFAULT 'planning',
    notes       TEXT,
    budget      DECIMAL(12, 2),
    currency    VARCHAR(10) NOT NULL DEFAULT 'VND',
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS trip_stops (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id     UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    day_number  INT NOT NULL,
    time        VARCHAR(50),
    place_name  VARCHAR(255),
    place_type  VARCHAR(50),
    address     VARCHAR(255),
    notes       TEXT,
    cost        DECIMAL(10, 2),
    currency    VARCHAR(10) NOT NULL DEFAULT 'VND',
    position    INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_memories_couple_id    ON memories(couple_id);
CREATE INDEX IF NOT EXISTS idx_memories_memory_date  ON memories(memory_date DESC);
CREATE INDEX IF NOT EXISTS idx_memory_media_memory   ON memory_media(memory_id);
CREATE INDEX IF NOT EXISTS idx_media_couple_id       ON media(couple_id);
CREATE INDEX IF NOT EXISTS idx_albums_couple_id      ON albums(couple_id);
CREATE INDEX IF NOT EXISTS idx_album_photos_album    ON album_photos(album_id);
CREATE INDEX IF NOT EXISTS idx_trips_couple_id       ON trips(couple_id);
CREATE INDEX IF NOT EXISTS idx_trips_start_date      ON trips(start_date DESC);
CREATE INDEX IF NOT EXISTS idx_trip_stops_trip       ON trip_stops(trip_id);
CREATE INDEX IF NOT EXISTS idx_trip_stops_day        ON trip_stops(trip_id, day_number);
