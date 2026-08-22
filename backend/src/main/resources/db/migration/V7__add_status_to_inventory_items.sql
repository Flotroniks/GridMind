ALTER TABLE inventory_items
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'IN_SERVICE'
        CHECK (status IN ('IN_SERVICE', 'OUT_OF_SERVICE'));

CREATE INDEX IF NOT EXISTS idx_inventory_items_status
    ON inventory_items (status);
