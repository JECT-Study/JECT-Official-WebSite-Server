ALTER TABLE mail_dispatch_job
    ADD COLUMN scheduled_at DATETIME(6) NULL;

ALTER TABLE mail_dispatch_target
    ADD COLUMN selection_result_snapshot VARCHAR(50) NULL;
