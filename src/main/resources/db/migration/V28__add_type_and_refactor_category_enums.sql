-- 1. type 컬럼 추가 (기본값은 '기타'인 'ETC' Enum 매핑값)
ALTER TABLE mail_scenario ADD COLUMN type VARCHAR(50) NOT NULL DEFAULT 'ETC';

-- 2. 기존 시나리오 레코드들의 category 값을 새 Enum(MailScenarioCategory) 스펙으로 마이그레이션
-- 기존: MEMBER, RECRUIT_ADDITIONAL -> CLUB_MEMBER
-- 기존: ADMIN_SUPPORT -> SUPPORTERS
-- 기존: GENERAL -> GENERAL
UPDATE mail_scenario SET category = 'CLUB_MEMBER' WHERE category IN ('MEMBER', 'RECRUIT_ADDITIONAL');
UPDATE mail_scenario SET category = 'SUPPORTERS' WHERE category = 'ADMIN_SUPPORT';
UPDATE mail_scenario SET category = 'GENERAL' WHERE category = 'GENERAL';
-- MAKERS는 그대로 MAKERS를 사용하므로 변경 불필요

-- 3. 기존 시나리오 중 특정 항목들에 대해 적절한 type 부여
UPDATE mail_scenario SET type = 'REJECT' WHERE name LIKE '%불합격%';
UPDATE mail_scenario SET type = 'FINAL_PASS' WHERE name LIKE '%최종 합격%';
UPDATE mail_scenario SET type = 'TEMP_PASS' WHERE name LIKE '%1차 합격%';
UPDATE mail_scenario SET type = 'STANDBY_PASS' WHERE name LIKE '%예비 합격%';
