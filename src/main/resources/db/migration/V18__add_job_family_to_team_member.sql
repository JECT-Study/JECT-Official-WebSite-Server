-- team_member 테이블에 job_family 컬럼 추가
ALTER TABLE team_member
    ADD COLUMN job_family VARCHAR(45) NULL;

-- 기존 member의 job_family 데이터를 team_member로 마이그레이션
UPDATE team_member tm
    JOIN member m ON tm.member_id = m.id
SET tm.job_family = m.job_family
WHERE m.job_family IS NOT NULL;
