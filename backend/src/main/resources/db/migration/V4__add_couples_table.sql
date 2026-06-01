CREATE TABLE IF NOT EXISTS couples (
    id           UUID PRIMARY KEY,
    name         VARCHAR(255) NOT NULL DEFAULT 'Us',
    start_date   DATE,
    anniversary  DATE,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO couples (id, name, start_date, anniversary)
VALUES ('11111111-1111-1111-1111-111111111111', 'Us', NULL, NULL)
ON CONFLICT (id) DO NOTHING;
