CREATE TABLE IF NOT EXISTS inventory_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 0)
);

CREATE INDEX IF NOT EXISTS idx_inventory_items_name
    ON inventory_items (name);
