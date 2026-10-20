ALTER TABLE mail_dispatch_job
    ADD COLUMN excluded_count INT NOT NULL DEFAULT 0,
    ADD INDEX idx_mail_dispatch_job_due (status, scheduled_at, id);

ALTER TABLE mail_dispatch_target
    ADD COLUMN exclusion_reason VARCHAR(50) NULL;
