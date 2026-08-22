CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE
);

INSERT INTO categories (name) VALUES
    ('Microcontrollers'),
    ('Electronic components'),
    ('Sensors'),
    ('Modules'),
    ('Displays'),
    ('Connectors'),
    ('Power supplies'),
    ('Mechanical parts'),
    ('Fasteners'),
    ('3D printing'),
    ('Tools'),
    ('Other');
