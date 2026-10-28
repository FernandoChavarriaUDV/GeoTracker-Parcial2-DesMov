CREATE TABLE locations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_locations_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT ck_location_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_location_longitude CHECK (longitude BETWEEN -180 AND 180),
    INDEX idx_locations_user (user_id, id)
);
