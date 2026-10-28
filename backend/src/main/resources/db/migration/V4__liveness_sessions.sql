CREATE TABLE liveness_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider_session_id VARCHAR(128) NOT NULL,
    region VARCHAR(40) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    status VARCHAR(30) NOT NULL,
    confidence DOUBLE NULL,
    CONSTRAINT fk_liveness_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX ix_liveness_user_created (user_id, created_at)
);
