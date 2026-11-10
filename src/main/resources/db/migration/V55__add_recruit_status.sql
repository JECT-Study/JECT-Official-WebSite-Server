ALTER TABLE recruit
    ADD COLUMN status VARCHAR(20) NULL;

-- 기존 공고는 모두 모집종료 상태로 이관
UPDATE recruit
SET status = 'CLOSED';

ALTER TABLE recruit
    MODIFY COLUMN status VARCHAR(20) NOT NULL;
