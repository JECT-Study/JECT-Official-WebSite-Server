ALTER TABLE apply
    ADD COLUMN submitted_at datetime(6) NULL;

-- 기존 제출 완료 지원서는 제출 시각이 없어 created_at을 임시 기준으로 백필한다.
UPDATE apply
SET submitted_at = created_at
WHERE status = 'SUBMITTED';
