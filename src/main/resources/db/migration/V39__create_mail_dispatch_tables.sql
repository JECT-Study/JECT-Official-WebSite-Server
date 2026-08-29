CREATE TABLE mail_dispatch_job
(
    id                   BIGINT AUTO_INCREMENT NOT NULL,
    scenario_id          BIGINT                NOT NULL,
    recruit_id           BIGINT                NOT NULL,
    requested_by_admin_id BIGINT                NOT NULL,
    idempotency_key      VARCHAR(255)          NOT NULL,
    status               VARCHAR(30)           NOT NULL,
    target_count         INT                   NOT NULL,
    processing_count     INT                   NOT NULL DEFAULT 0,
    success_count        INT                   NOT NULL DEFAULT 0,
    failed_count         INT                   NOT NULL DEFAULT 0,
    requested_at         datetime(6)           NOT NULL,
    subject_template     TEXT                  NOT NULL,
    body_template        TEXT                  NOT NULL,
    input_variables_json TEXT,
    started_at           datetime(6),
    finished_at          datetime(6),
    version              BIGINT                NOT NULL DEFAULT 0,
    created_at           datetime(6),
    updated_at           datetime(6),
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT uk_mail_dispatch_job_requester_key UNIQUE (requested_by_admin_id, idempotency_key)
) ENGINE = InnoDB;

CREATE TABLE mail_dispatch_target
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    dispatch_job_id BIGINT                NOT NULL,
    apply_id        BIGINT                NOT NULL,
    email           VARCHAR(255)          NOT NULL,
    status          VARCHAR(30)           NOT NULL,
    failure_reason  TEXT,
    sent_at         datetime(6),
    version         BIGINT                NOT NULL DEFAULT 0,
    created_at      datetime(6),
    updated_at      datetime(6),
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT fk_mail_dispatch_target_job
        FOREIGN KEY (dispatch_job_id) REFERENCES mail_dispatch_job (id) ON DELETE CASCADE,
    CONSTRAINT uk_mail_dispatch_target_job_apply UNIQUE (dispatch_job_id, apply_id)
) ENGINE = InnoDB;

CREATE INDEX idx_mail_dispatch_target_job_status
    ON mail_dispatch_target (dispatch_job_id, status);
