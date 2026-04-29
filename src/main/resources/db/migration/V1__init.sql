CREATE TABLE IF NOT EXISTS backend_server (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    environment VARCHAR(10)  NOT NULL,
    version     VARCHAR(50),
    jnlp_url    VARCHAR(500),
    check_url   VARCHAR(500),
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_backend_server_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS game_association (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_name         VARCHAR(255) NOT NULL,
    backend_server_id BIGINT       NOT NULL,
    environment       VARCHAR(10)  NOT NULL,
    CONSTRAINT fk_game_assoc_backend FOREIGN KEY (backend_server_id)
        REFERENCES backend_server (id) ON DELETE CASCADE
);
