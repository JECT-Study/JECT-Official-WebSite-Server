ALTER TABLE mail_dispatch_job
    ADD COLUMN request_fingerprint TEXT NULL AFTER input_variables_json;
