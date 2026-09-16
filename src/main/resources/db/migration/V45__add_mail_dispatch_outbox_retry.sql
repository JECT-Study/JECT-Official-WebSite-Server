ALTER TABLE mail_dispatch_outbox
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN next_attempt_at datetime(6),
    ADD COLUMN last_failure_reason TEXT;
