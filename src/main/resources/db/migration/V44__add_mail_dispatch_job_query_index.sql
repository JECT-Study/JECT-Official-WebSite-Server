CREATE INDEX idx_mail_dispatch_job_admin_requested_at
    ON mail_dispatch_job (requested_by_admin_id, requested_at, id);
