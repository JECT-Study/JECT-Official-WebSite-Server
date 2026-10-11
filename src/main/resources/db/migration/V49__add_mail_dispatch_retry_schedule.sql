ALTER TABLE mail_dispatch_outbox
    ADD COLUMN attempt_count INT NOT NULL DEFAULT 0,
    ADD COLUMN next_attempt_at DATETIME(6) NULL;

-- 기존 claim은 이미 획득된 시도이며 미확정 상태를 재시도 대기로 바꾸지 않는다.
UPDATE mail_dispatch_outbox
SET attempt_count = 1
WHERE claim_token IS NOT NULL;

CREATE INDEX idx_mail_dispatch_outbox_status_next_attempt_id
    ON mail_dispatch_outbox (status, next_attempt_at, id);
