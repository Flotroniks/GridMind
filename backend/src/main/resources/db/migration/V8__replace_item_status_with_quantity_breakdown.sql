DROP INDEX IF EXISTS idx_inventory_items_status;

ALTER TABLE inventory_items
    DROP COLUMN status;

ALTER TABLE inventory_items
    ADD COLUMN quantity_hs INTEGER NOT NULL DEFAULT 0 CHECK (quantity_hs >= 0),
    ADD COLUMN quantity_in_use INTEGER NOT NULL DEFAULT 0 CHECK (quantity_in_use >= 0),
    ADD CONSTRAINT chk_inventory_items_quantity_breakdown
        CHECK (quantity_hs + quantity_in_use <= quantity);
