-- recruit 테이블에 recruit_type 컬럼 추가 (모집 단위)
-- REGULAR: 정규 모집, REGULAR_ADDITIONAL: 정규 모집 - 추가합격
-- EXISTING_MEMBER: 기존 기수 모집, SEPARATE: 별도 합류
ALTER TABLE recruit
    ADD COLUMN recruit_type VARCHAR(45) NULL;
