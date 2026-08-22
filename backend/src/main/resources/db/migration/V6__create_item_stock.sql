CREATE TABLE IF NOT EXISTS item_stock (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES inventory_items(id) ON DELETE CASCADE,
    storage_location_id BIGINT NOT NULL REFERENCES storage_locations(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    UNIQUE (item_id, storage_location_id)
);

CREATE INDEX IF NOT EXISTS idx_item_stock_item_id
    ON item_stock (item_id);

CREATE INDEX IF NOT EXISTS idx_item_stock_storage_location_id
    ON item_stock (storage_location_id);
