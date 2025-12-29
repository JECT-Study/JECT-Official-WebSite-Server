-- member table 의 role type 변경 쿼리
-- USER -> SEMESTER
UPDATE member SET role = 'SEMESTER' WHERE role = 'USER';
-- TEMP -> APPLY
UPDATE member SET role = 'APPLY' WHERE role = 'TEMP';
