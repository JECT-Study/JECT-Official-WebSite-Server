ALTER TABLE mail_dispatch_outbox
    ADD COLUMN claimed_by VARCHAR(100),
    ADD COLUMN lease_until datetime(6);
