-- 삭제 이력은 유지하고 활성 지원에만 지원자·모집 중복 제한 적용
ALTER TABLE apply
    ADD COLUMN active_unique_key TINYINT
        GENERATED ALWAYS AS (IF(is_deleted = 0, 1, NULL)) VIRTUAL,
    DROP INDEX uk_apply_applicant_recruit,
    ADD CONSTRAINT uk_apply_applicant_recruit_active
        UNIQUE (applicant_id, recruit_id, active_unique_key);
