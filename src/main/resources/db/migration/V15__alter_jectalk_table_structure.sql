-- Jectalk 테이블 구조 변경
-- 기존: name, summary, youtube_url, image_url
-- 변경: title, description, content_type, content_url, thumbnail_url, author

-- author 컬럼 추가
ALTER TABLE jectalk ADD COLUMN author VARCHAR(255) NOT NULL DEFAULT '';

-- 기존 summary 데이터를 author로 마이그레이션
UPDATE jectalk SET author = summary;

-- content_type 컬럼 추가
ALTER TABLE jectalk ADD COLUMN content_type VARCHAR(50) NOT NULL DEFAULT '';

-- 컬럼명 변경
ALTER TABLE jectalk CHANGE COLUMN name title VARCHAR(50) NOT NULL;
ALTER TABLE jectalk CHANGE COLUMN summary description VARCHAR(255) NOT NULL;
ALTER TABLE jectalk CHANGE COLUMN youtube_url content_url VARCHAR(2083) NULL;
ALTER TABLE jectalk CHANGE COLUMN image_url thumbnail_url VARCHAR(2083) NULL;

-- author 컬럼의 DEFAULT 제거
ALTER TABLE jectalk MODIFY COLUMN author VARCHAR(255) NOT NULL;
ALTER TABLE jectalk MODIFY COLUMN content_type VARCHAR(50) NOT NULL;

