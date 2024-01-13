DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE IF NOT EXISTS categories
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       varchar(50),
    UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS users
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(250),
    email varchar(254),
        UNIQUE (email)
)
