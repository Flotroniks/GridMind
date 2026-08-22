CREATE TABLE IF NOT EXISTS item_tags (
    item_id BIGINT NOT NULL REFERENCES inventory_items(id) ON DELETE CASCADE,
    tag VARCHAR(40) NOT NULL,
    PRIMARY KEY (item_id, tag)
);
