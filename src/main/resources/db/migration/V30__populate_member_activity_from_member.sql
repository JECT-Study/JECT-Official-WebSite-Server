-- member 테이블에서 1기-4기 일반 회원 데이터를 member_activity와 member_semester로 복사
-- 1기-3기 구성원: is_deleted=false, role=SEMESTER, status=ACTIVE -> COMPLETED상태로 복사
-- 4기 구성원: is_deleted=false, role=SEMESTER, status=ACTIVE -> ACTIVE상태로 복사
-- 4기는 role 승격이 이루어져있지 않아 데이터 이관 불가
    -- 운영 배포 전까지 4기 구성원의 role 승격 필요

-- 활동 데이터 복사
INSERT INTO member_activity (
    created_at,
    updated_at,
    member_id,
    career_details,
    member_type,
    job_family,
    activity_status,
    experience_period
)
SELECT DISTINCT
    now(),
    now(),
    m.id,
    m.career_details,
    'SEMESTER',
    m.job_family,
    CASE
        WHEN s.semester_name = '4기' THEN 'ACTIVE'
        ELSE 'COMPLETED'
    END,
    m.experience_period
FROM member m
JOIN semester s ON s.id = m.semester_id
WHERE m.is_deleted = false
  AND m.role = 'SEMESTER'
  AND m.status = 'ACTIVE'
  AND s.semester_name IN ('1기', '2기', '3기', '4기');

-- 일반 구성원의 전용 관리 항목 복사
INSERT INTO member_semester (
    created_at,
    updated_at,
    id,
    semester_id,
    team_id
)
SELECT
    now(),
    now(),
    ma.id,
    m.semester_id,
    mt.team_id
FROM member_activity ma
JOIN member m ON m.id = ma.member_id
LEFT JOIN (
    SELECT
        member_id,
        MAX(team_id) AS team_id
    FROM team_member
    GROUP BY member_id
) mt ON mt.member_id = ma.member_id
WHERE ma.member_type = 'SEMESTER';
