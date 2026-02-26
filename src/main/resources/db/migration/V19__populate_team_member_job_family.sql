-- 기존 member.job_family 데이터를 team_member.job_family로 마이그레이션
UPDATE team_member tm
JOIN member m ON tm.member_id = m.id
SET tm.job_family = m.job_family
WHERE tm.job_family IS NULL AND m.job_family IS NOT NULL;
