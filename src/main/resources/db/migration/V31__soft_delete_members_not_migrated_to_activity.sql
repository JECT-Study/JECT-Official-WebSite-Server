-- 활동 관리 데이터로 이관되지 않은 불필요한 member 데이터를 soft delete 처리
UPDATE member m
LEFT JOIN member_activity ma ON ma.member_id = m.id
SET m.is_deleted = true
WHERE ma.id IS NULL
  AND m.is_deleted = false
