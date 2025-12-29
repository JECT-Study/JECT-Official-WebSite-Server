-- member 테이블에 status 컬럼 추가
ALTER TABLE member ADD COLUMN status VARCHAR(45) NOT NULL DEFAULT 'ACTIVE';
