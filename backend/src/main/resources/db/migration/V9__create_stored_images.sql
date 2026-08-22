CREATE TABLE IF NOT EXISTS stored_images (
    id BIGSERIAL PRIMARY KEY,
    checksum VARCHAR(64) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    source_provider VARCHAR(120),
    source_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE inventory_items
    ADD COLUMN image_id BIGINT REFERENCES stored_images(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_inventory_items_image_id
    ON inventory_items (image_id);
