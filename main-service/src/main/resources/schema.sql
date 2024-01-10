DROP TABLE IF EXISTS categories CASCADE;

CREATE TABLE IF NOT EXISTS categories
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       varchar(50),
    UNIQUE (name)
);
