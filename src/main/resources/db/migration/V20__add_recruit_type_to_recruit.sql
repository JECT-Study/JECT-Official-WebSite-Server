-- recruit 테이블에 recruit_type 컬럼 추가 (모집 단위)
-- REGULAR: 정규 모집, REGULAR_WAITLIST: 정규 모집 - 추가합격
-- BACKFILL: 기존 기수 모집, MANUAL: 별도 합류
ALTER TABLE recruit
    ADD COLUMN recruit_type VARCHAR(45) NULL;

-- 기존 레코드 REGULAR로 초기화
UPDATE recruit SET recruit_type = 'REGULAR' WHERE recruit_type IS NULL;

-- NOT NULL 제약 추가
ALTER TABLE recruit
    MODIFY COLUMN recruit_type VARCHAR(45) NOT NULL;
