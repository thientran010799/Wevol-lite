ALTER TABLE memories ADD COLUMN IF NOT EXISTS search_vector tsvector;
ALTER TABLE trips    ADD COLUMN IF NOT EXISTS search_vector tsvector;

CREATE INDEX IF NOT EXISTS idx_memories_search ON memories USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_trips_search    ON trips    USING GIN (search_vector);

CREATE OR REPLACE FUNCTION update_memory_search_vector() RETURNS TRIGGER AS $$
BEGIN
  NEW.search_vector := to_tsvector('english',
    COALESCE(NEW.title, '') || ' ' ||
    COALESCE(NEW.note, '') || ' ' ||
    COALESCE(NEW.location, '')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER memories_search_vector_update
BEFORE INSERT OR UPDATE ON memories
FOR EACH ROW EXECUTE FUNCTION update_memory_search_vector();

CREATE OR REPLACE FUNCTION update_trip_search_vector() RETURNS TRIGGER AS $$
BEGIN
  NEW.search_vector := to_tsvector('english',
    COALESCE(NEW.name, '') || ' ' ||
    COALESCE(NEW.notes, '')
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trips_search_vector_update
BEFORE INSERT OR UPDATE ON trips
FOR EACH ROW EXECUTE FUNCTION update_trip_search_vector();

UPDATE memories SET search_vector = to_tsvector('english',
    COALESCE(title, '') || ' ' || COALESCE(note, '') || ' ' || COALESCE(location, ''));

UPDATE trips SET search_vector = to_tsvector('english',
    COALESCE(name, '') || ' ' || COALESCE(notes, ''));
