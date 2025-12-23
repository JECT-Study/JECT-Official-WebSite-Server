-- project_intro 테이블의 category 컬럼 마이그레이션
-- SERVICE -> SAMPLE
UPDATE project_intro
SET category = 'SAMPLE'
WHERE category = 'SERVICE';

-- DEV -> DESCRIPTION
UPDATE project_intro
SET category = 'DESCRIPTION'
WHERE category = 'DEV';

-- project 테이블에 serviceType 필드 추가
ALTER TABLE project ADD COLUMN serviceType VARCHAR(50) NOT NULL DEFAULT 'WEB';
