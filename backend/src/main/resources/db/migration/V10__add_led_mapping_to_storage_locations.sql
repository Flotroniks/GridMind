ALTER TABLE storage_locations
    ADD COLUMN led_controller_id VARCHAR(60),
    ADD COLUMN led_index INT;
