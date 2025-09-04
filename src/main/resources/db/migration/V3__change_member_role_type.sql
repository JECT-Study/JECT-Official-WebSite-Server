-- member table 의 role type 변경 쿼리
-- USER -> CORE
UPDATE member SET role = 'CORE' WHERE role = 'USER';
-- TEMP -> RECRUIT
UPDATE member SET role = 'RECRUIT' WHERE role = 'TEMP';
