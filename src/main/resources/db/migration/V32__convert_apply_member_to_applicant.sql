-- 지원도메인에서 member_id -> applicant_id로 컬럼,제약조건 변경
ALTER TABLE apply
    DROP FOREIGN KEY fk_apply_member;

ALTER TABLE apply
    DROP INDEX uk_apply_member_recruit;

ALTER TABLE apply
    CHANGE COLUMN member_id applicant_id BIGINT NOT NULL;

ALTER TABLE apply
    ADD CONSTRAINT fk_apply_applicant FOREIGN KEY (applicant_id) REFERENCES applicant (id) ON DELETE NO ACTION;

ALTER TABLE apply
    ADD CONSTRAINT uk_apply_applicant_recruit UNIQUE (applicant_id, recruit_id);
