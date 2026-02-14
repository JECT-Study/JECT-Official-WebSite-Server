-- 1. team_member 테이블에 job_family 컬럼 추가
ALTER TABLE team_member ADD COLUMN job_family varchar(45);

-- 2. 멤버가 속한 기수의 '미배정' 팀이 없으면 생성
INSERT INTO team (semester_id, name, created_at, updated_at)
SELECT distinct m.semester_id, '미배정', NOW(), NOW()
FROM member m
WHERE m.semester_id IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM team t WHERE t.semester_id = m.semester_id AND t.name = '미배정');

-- 3. 기존 team_member가 있는 멤버들의 job_family 업데이트
UPDATE team_member tm
JOIN member m ON tm.member_id = m.id
SET tm.job_family = m.job_family
WHERE m.job_family IS NOT NULL;

-- 4. 팀이 없는 멤버들을 '미배정' 팀에 연결하여 team_member 생성
INSERT INTO team_member (member_id, team_id, job_family, created_at, updated_at)
SELECT m.id, t.id, m.job_family, m.created_at, m.updated_at
FROM member m
JOIN team t ON m.semester_id = t.semester_id AND t.name = '미배정'
WHERE m.semester_id IS NOT NULL AND m.job_family IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM team_member tm WHERE tm.member_id = m.id);

-- 5. member 테이블에서 semester_id, job_family 컬럼 삭제
ALTER TABLE member DROP COLUMN semester_id;
ALTER TABLE member DROP COLUMN job_family;
