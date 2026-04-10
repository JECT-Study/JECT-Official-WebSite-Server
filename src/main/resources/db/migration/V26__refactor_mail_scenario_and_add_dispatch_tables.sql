ALTER TABLE mail_scenario
    ADD COLUMN scenario_code VARCHAR(100) NULL AFTER category,
    ADD COLUMN subject_template TEXT NULL AFTER scenario_code,
    ADD COLUMN body_template TEXT NULL AFTER subject_template,
    ADD COLUMN active TINYINT(1) NOT NULL DEFAULT 1 AFTER body_template;

UPDATE mail_scenario
SET scenario_code = CASE
    WHEN name = '일반 구성원 - 불합격 통지' THEN 'MEMBER_REJECT_NOTICE'
    ELSE CONCAT('LEGACY_SCENARIO_', id)
END
WHERE scenario_code IS NULL OR scenario_code = '';

UPDATE mail_scenario
SET subject_template = COALESCE(NULLIF(subject_template, ''), name),
    body_template = COALESCE(NULLIF(body_template, ''), COALESCE(code, ''));

ALTER TABLE mail_scenario
    MODIFY COLUMN scenario_code VARCHAR(100) NOT NULL,
    MODIFY COLUMN subject_template TEXT NOT NULL,
    MODIFY COLUMN body_template TEXT NOT NULL;

CREATE UNIQUE INDEX uk_mail_scenario_scenario_code ON mail_scenario (scenario_code);

ALTER TABLE mail_scenario
    DROP COLUMN code;

ALTER TABLE mail_scenario_variables
    CHANGE COLUMN variables variable VARCHAR(50) NOT NULL;

CREATE TABLE IF NOT EXISTS mail_dispatch_job
(
    id                    BIGINT AUTO_INCREMENT NOT NULL,
    scenario_id           BIGINT                NOT NULL,
    requested_by_member_id BIGINT               NULL,
    status                VARCHAR(30)           NOT NULL,
    receiver_count        INT                   NOT NULL DEFAULT 0,
    common_variables_json TEXT                  NULL,
    started_at            datetime(6)           NULL,
    finished_at           datetime(6)           NULL,
    created_at            datetime(6)           NULL,
    updated_at            datetime(6)           NULL,
    CONSTRAINT PRIMARY KEY (id),
    CONSTRAINT fk_mail_dispatch_job_scenario FOREIGN KEY (scenario_id) REFERENCES mail_scenario (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS mail_dispatch_target
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    job_id         BIGINT                NOT NULL,
    receiver_id    BIGINT                NULL,
    email          VARCHAR(255)          NOT NULL,
    status         VARCHAR(30)           NOT NULL,
    failure_reason TEXT                  NULL,
    sent_at        datetime(6)           NULL,
    created_at     datetime(6)           NULL,
    updated_at     datetime(6)           NULL,
    CONSTRAINT PRIMARY KEY (id),
    CONSTRAINT fk_mail_dispatch_target_job FOREIGN KEY (job_id) REFERENCES mail_dispatch_job (id)
) ENGINE = InnoDB;

CREATE INDEX idx_mail_dispatch_target_job_status ON mail_dispatch_target (job_id, status);
