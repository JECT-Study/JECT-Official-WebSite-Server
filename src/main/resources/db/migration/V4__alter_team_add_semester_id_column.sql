-- team 테이블에 semester_id 컬럼 추가
ALTER TABLE team ADD COLUMN semester_id BIGINT NOT NULL;

-- team 테이블의 semester_id 컬럼을 project 테이블의 semester_id 값으로 초기화
UPDATE team
    JOIN project ON team.id = project.team_id
    SET team.semester_id = project.semester_id;

-- project 테이블에 semester_id 컬럼 삭제
ALTER TABLE project DROP COLUMN semester_id;
