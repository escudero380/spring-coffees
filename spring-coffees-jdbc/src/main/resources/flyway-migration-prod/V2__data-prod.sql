/* Pre-populates database with necessary data. */

-- inserts default 'admin' user (if there is no one) with the encoded 'admin' password
INSERT INTO users (username, first_name, last_name, password, enabled, authority, email)
SELECT *
FROM (VALUES ('admin', 'Admin', 'Admin', '$2a$10$CLM.npM.IIIQFz/0XYG1UOlPe12MabEMzDyJQMqSYqN0mmNlUb6UW',
              TRUE, 'ADMIN', 'tepac31407@intsv.net')) AS tmp
WHERE NOT EXISTS(SELECT * FROM users where authority = 'ADMIN');