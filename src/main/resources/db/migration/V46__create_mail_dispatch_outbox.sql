CREATE TABLE mail_dispatch_outbox
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    dispatch_job_id BIGINT                NOT NULL,
    apply_id        BIGINT                NOT NULL,
    email           VARCHAR(255)          NOT NULL,
    subject         TEXT                  NOT NULL,
    body            TEXT                  NOT NULL,
    status          VARCHAR(30)           NOT NULL,
    failure_reason  TEXT,
    version         BIGINT                NOT NULL DEFAULT 0,
    created_at      datetime(6),
    updated_at      datetime(6),
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT fk_mail_dispatch_outbox_job
        FOREIGN KEY (dispatch_job_id) REFERENCES mail_dispatch_job (id) ON DELETE CASCADE,
    CONSTRAINT uk_mail_dispatch_outbox_job_apply UNIQUE (dispatch_job_id, apply_id)
) ENGINE = InnoDB;

CREATE INDEX idx_mail_dispatch_outbox_status_id
    ON mail_dispatch_outbox (status, id);
