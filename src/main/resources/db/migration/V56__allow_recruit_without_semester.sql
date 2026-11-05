-- 일반 구성원 외 모집공고는 기수 없이 등록
ALTER TABLE recruit
    DROP FOREIGN KEY fk_recruit_semester;

ALTER TABLE recruit
    DROP INDEX uq_recruit_semester_job_family,
    MODIFY COLUMN semester_id BIGINT NULL;

ALTER TABLE recruit
    ADD CONSTRAINT fk_recruit_semester FOREIGN KEY (semester_id) REFERENCES semester (id);

-- 미종료 공고에만 유형·기수·직군 조합의 중복 제한 적용
ALTER TABLE recruit
    ADD COLUMN open_unique_key VARCHAR(120)
        GENERATED ALWAYS AS (
            IF(status = 'OPEN', CONCAT(recruit_type, ':', IFNULL(semester_id, 0), ':', job_family), NULL)
        ) VIRTUAL,
    ADD CONSTRAINT uk_recruit_open UNIQUE (open_unique_key);
