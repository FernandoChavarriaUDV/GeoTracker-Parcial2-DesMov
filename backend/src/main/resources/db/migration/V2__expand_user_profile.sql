ALTER TABLE users
    ADD COLUMN last_name VARCHAR(120) NOT NULL DEFAULT '' AFTER name,
    ADD COLUMN username VARCHAR(180) NULL AFTER last_name,
    ADD COLUMN gender VARCHAR(30) NULL AFTER password_hash,
    ADD COLUMN birth_date DATE NULL AFTER gender,
    ADD COLUMN phone VARCHAR(30) NULL AFTER birth_date,
    ADD COLUMN avatar_base64 LONGTEXT NULL AFTER phone;

UPDATE users SET username = email WHERE username IS NULL;

ALTER TABLE users
    MODIFY COLUMN username VARCHAR(180) NOT NULL,
    ADD CONSTRAINT uk_users_username UNIQUE (username);
