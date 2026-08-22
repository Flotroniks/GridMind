CREATE TABLE IF NOT EXISTS storage_locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    parent_id BIGINT REFERENCES storage_locations(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_storage_locations_parent_id
    ON storage_locations (parent_id);
