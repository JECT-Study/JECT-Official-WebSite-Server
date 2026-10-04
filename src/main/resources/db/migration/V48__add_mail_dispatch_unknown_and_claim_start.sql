ALTER TABLE mail_dispatch_job
    ADD COLUMN unknown_count INT NOT NULL DEFAULT 0,
    ADD COLUMN claim_started_at DATETIME(6) NULL;
