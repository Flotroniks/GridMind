ALTER TABLE inventory_items
    ADD COLUMN description TEXT,
    ADD COLUMN manufacturer VARCHAR(120),
    ADD COLUMN reference VARCHAR(120),
    ADD COLUMN category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    ADD COLUMN notes TEXT,
    ADD COLUMN product_url VARCHAR(500),
    ADD COLUMN datasheet_url VARCHAR(500),
    ADD COLUMN minimum_quantity INTEGER NOT NULL DEFAULT 0 CHECK (minimum_quantity >= 0);

CREATE INDEX IF NOT EXISTS idx_inventory_items_manufacturer
    ON inventory_items (manufacturer);

CREATE INDEX IF NOT EXISTS idx_inventory_items_category_id
    ON inventory_items (category_id);
