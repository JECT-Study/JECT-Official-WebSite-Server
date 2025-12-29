-- project 테이블에 badges 컬럼 추가
ALTER TABLE project
    ADD COLUMN badges VARCHAR(255) NULL COMMENT '프로젝트 뱃지/태그 (콤마 구분)';

