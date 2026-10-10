ALTER TABLE mail_dispatch_outbox
    ADD COLUMN claim_token VARCHAR(36) NULL,
    ADD COLUMN lease_until DATETIME(6) NULL;

CREATE INDEX idx_mail_dispatch_outbox_status_lease_id
    ON mail_dispatch_outbox (status, lease_until, id);
