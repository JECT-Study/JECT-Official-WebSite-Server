CREATE TABLE change_history
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at  DATETIME(6)  NULL,
    updated_at  DATETIME(6)  NULL,
    domain      VARCHAR(50)  NOT NULL,
    action_type VARCHAR(20)  NOT NULL,
    target_id   VARCHAR(100) NULL,
    actor_id    BIGINT       NULL
);

CREATE INDEX idx_change_history_domain_target ON change_history (domain, target_id);

CREATE TABLE change_detail
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    change_history_id  BIGINT       NOT NULL,
    entity_name        VARCHAR(100) NOT NULL,
    entity_id          VARCHAR(100) NULL,
    action_type        VARCHAR(20)  NOT NULL,
    before_data        TEXT         NULL,
    after_data         TEXT         NULL,
    CONSTRAINT fk_change_detail_change_history
        FOREIGN KEY (change_history_id) REFERENCES change_history (id)
);

CREATE INDEX idx_change_detail_change_history_id ON change_detail (change_history_id);
