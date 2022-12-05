/* Pre-populates database with demonstration data. */

-- inserts default 'admin' user (if there is no one) with the encoded 'admin' password
INSERT INTO users (username, first_name, last_name, password, enabled, authority, email)
SELECT *
FROM (VALUES ('admin', 'Admin', 'Admin', '$2a$10$CLM.npM.IIIQFz/0XYG1UOlPe12MabEMzDyJQMqSYqN0mmNlUb6UW',
              TRUE, 'ADMIN', 'tepac31407@intsv.net')) AS tmp
WHERE NOT EXISTS(SELECT * FROM users where authority = 'ADMIN');

INSERT INTO suppliers (name, email, address)
VALUES ('Coffee Millers', 'contact@cmiller.com', 'Crusader House 791, Kampala'),
       ('Star Coffee', 'sales@star-coffe.br', 'Rua Agricola 686, Sao Paulo'),
       ('The High Grounds', 'manager@thg-group.com', 'Hing Road 12, Hong Kong'),
       ('Colombia Supremo', 'info@csucolombia.com', 'Calle 28-13A, Bogota'),
       ('Dummy Supplier', 'dummy@dummy.com', 'Dummy 1-22, Dummy');

INSERT INTO coffees (name, origin, supplier_id, current_price, stock)
VALUES ('Arusha', 'Tanzania', 1, 18.95, 9000),
       ('Catuai', 'Brazil', 2, 23.20, 12000),
       ('Catuai', 'Columbia', 4, 22.50, 6000),
       ('Mocha', 'Tanzania', 1, 34.85, 3500),
       ('Pacas', 'Brazil', 2, 20.90, 10000),
       ('Pacas', 'Columbia', 4, 19.45, 8500),
       ('Typica', 'Indonesia', 3, 17.00, 15000),
       ('Catuai', 'Brazil', 3, 21.50, 4000),
       ('Mocha', 'Tanzania', 2, 31.00, 7200),
       ('Dummy', 'Dummy', 2, 150.00, 10000);

-- inserts demo users with the encoded '12345678' password for all of them
INSERT INTO users (username, first_name, last_name, password, enabled, authority, email)
VALUES ('alex', 'Alex', 'Miller', '$2a$10$YpFdq2.vqjb87VXhHoL2P.znYMDGDkv5jVNTmUnhQMZglX0W6squ.', TRUE,
        'MANAGER', 'cogajar530@xenzld.com'),
       ('peter', 'Peter', 'Brown', '$2a$10$YpFdq2.vqjb87VXhHoL2P.znYMDGDkv5jVNTmUnhQMZglX0W6squ.', TRUE,
        'MANAGER', 'cogajar531@xenzld.com'),
       ('alice', 'Alice', 'Mercia', '$2a$10$YpFdq2.vqjb87VXhHoL2P.znYMDGDkv5jVNTmUnhQMZglX0W6squ.', FALSE,
        'MANAGER', 'cogajar532@xenzld.com'),
       ('jane', 'Jane', 'Smith', '$2a$10$YpFdq2.vqjb87VXhHoL2P.znYMDGDkv5jVNTmUnhQMZglX0W6squ.', TRUE,
        'MANAGER', 'cogajar533@xenzld.com'),
       ('dummy', 'Bob', 'Dummy', '$2a$10$YpFdq2.vqjb87VXhHoL2P.znYMDGDkv5jVNTmUnhQMZglX0W6squ.', FALSE,
        'NEWCOMER', 'cogajar534@xenzld.com');


INSERT INTO sales (coffee_id, manager, datetime, sale_quantity, sale_sum)
VALUES (1, 'alex', CURRENT_TIMESTAMP - 1, 250, 4.74),
       (4, 'alex', CURRENT_TIMESTAMP - 2, 350, 8.12),
       (1, 'alex', CURRENT_TIMESTAMP - 3, 250, 4.74),
       (1, 'alex', CURRENT_TIMESTAMP - 6, 250, 4.74),
       (4, 'jane', CURRENT_TIMESTAMP - 4, 350, 8.12),
       (1, 'jane', CURRENT_TIMESTAMP - 5, 250, 4.74),
       (2, 'peter', CURRENT_TIMESTAMP - 32, 750, 17.14),
       (1, 'peter', CURRENT_TIMESTAMP - 33, 250, 4.74),
       (3, 'alex', CURRENT_TIMESTAMP - 34, 490, 11.03),
       (5, 'alex', CURRENT_TIMESTAMP - 35, 950, 19.86),
       (1, 'alex', CURRENT_TIMESTAMP - 36, 250, 4.74),
       (9, 'jane', CURRENT_TIMESTAMP - 11, 250, 7.75),
       (6, 'jane', CURRENT_TIMESTAMP - 12, 800, 15.56),
       (2, 'peter', CURRENT_TIMESTAMP - 8, 750, 17.14),
       (7, 'peter', CURRENT_TIMESTAMP - 9, 1000, 17.00),
       (6, 'peter', CURRENT_TIMESTAMP - 10, 800, 15.56);