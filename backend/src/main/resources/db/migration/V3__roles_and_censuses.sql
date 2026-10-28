ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'CENSISTA';
ALTER TABLE users ADD CONSTRAINT ck_users_role CHECK (role IN ('CENSISTA', 'ADMINISTRADOR'));

CREATE TABLE censuses (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    total_members INT NOT NULL,
    television_count INT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    origin VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    censist_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_censuses_user FOREIGN KEY (censist_id) REFERENCES users(id),
    CONSTRAINT ck_censuses_members CHECK (total_members BETWEEN 1 AND 1000),
    CONSTRAINT ck_censuses_televisions CHECK (television_count BETWEEN 0 AND 1000),
    CONSTRAINT ck_censuses_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_censuses_longitude CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT ck_censuses_origin CHECK (origin IN ('NORMAL', 'MASIVO')),
    INDEX idx_censuses_origin_id (origin, id)
);
