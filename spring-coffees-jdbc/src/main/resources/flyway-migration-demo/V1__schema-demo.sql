/* Defines database schema (for the embedded H2 DBMS) */

DROP TABLE IF EXISTS suppliers CASCADE;
DROP TABLE IF EXISTS coffees CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS sales;

CREATE TABLE IF NOT EXISTS suppliers
(
    id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name    VARCHAR(50)  NOT NULL,
    email   VARCHAR(254) NOT NULL UNIQUE,
    address VARCHAR(250) NOT NULL
);

CREATE TABLE IF NOT EXISTS coffees
(
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(50)   NOT NULL,
    origin        VARCHAR(20)   NOT NULL,
    supplier_id   INT           NOT NULL,
    current_price DECIMAL(5, 2) NOT NULL,
    stock         INT           NOT NULL
);

CREATE TABLE IF NOT EXISTS users
(
    username    VARCHAR(25) PRIMARY KEY,
    first_name  VARCHAR(20)  NOT NULL,
    last_name   VARCHAR(20)  NOT NULL,
    password    VARCHAR(250) NOT NULL,
    enabled     BOOLEAN      NOT NULL,
    authority   VARCHAR(10)  NOT NULL,
    email       VARCHAR(254) NOT NULL UNIQUE,
    reset_token CHAR(36),
    exp_before  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sales
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    coffee_id     INT           NOT NULL,
    manager       VARCHAR(25)   NOT NULL,
    datetime      TIMESTAMP     NOT NULL,
    sale_quantity INT           NOT NULL,
    sale_sum      DECIMAL(9, 2) NOT NULL
);

ALTER TABLE coffees
    ADD FOREIGN KEY (supplier_id) REFERENCES suppliers (id)
        ON UPDATE CASCADE ON DELETE RESTRICT;
ALTER TABLE sales
    ADD FOREIGN KEY (coffee_id) REFERENCES coffees (id)
        ON UPDATE CASCADE ON DELETE RESTRICT;
ALTER TABLE sales
    ADD FOREIGN KEY (manager) REFERENCES users (username)
        ON UPDATE CASCADE ON DELETE RESTRICT;