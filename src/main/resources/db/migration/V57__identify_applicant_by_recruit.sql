-- 기수 없는 공고를 지원할 수 있도록 공고 식별값 추가
ALTER TABLE applicant
    MODIFY COLUMN semester_id BIGINT NULL,
    ADD COLUMN recruit_id BIGINT NULL,
    ADD CONSTRAINT fk_applicant_recruit FOREIGN KEY (recruit_id) REFERENCES recruit (id);

-- 기존 지원 이력에서 공고가 하나로 확인되는 지원자만 연결하고 나머지는 원본 보존
UPDATE applicant a
JOIN (
    SELECT applicant_id, MIN(recruit_id) AS recruit_id
    FROM apply
    GROUP BY applicant_id
    HAVING COUNT(DISTINCT recruit_id) = 1
) history ON history.applicant_id = a.id
JOIN recruit r ON r.id = history.recruit_id
SET a.recruit_id = r.id,
    a.semester_id = r.semester_id,
    a.member_type = CASE
        WHEN r.recruit_type IN ('MAKERS', 'SUPPORTERS') THEN r.recruit_type
        ELSE 'SEMESTER'
    END
WHERE a.role NOT IN ('ADMIN', 'OPERATIONS', 'SUPPORTER');

-- 삭제 이력과 공고가 없는 기존 계정은 유지하고 활성 공고별 이메일만 중복 제한
ALTER TABLE applicant
    ADD COLUMN active_unique_key TINYINT
        GENERATED ALWAYS AS (IF(is_deleted = 0, 1, NULL)) VIRTUAL,
    ADD CONSTRAINT uk_applicant_email_recruit_active UNIQUE (email, recruit_id, active_unique_key);
