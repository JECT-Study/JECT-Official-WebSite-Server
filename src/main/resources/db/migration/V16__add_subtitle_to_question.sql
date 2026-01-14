-- Question 테이블에 subtitle 컬럼 추가
ALTER TABLE question ADD COLUMN subtitle VARCHAR(200) NULL;

-- semester_id가 4이고 job_family가 PD인 recruit의 question 중에서
-- title이 "포트폴리오가 있으시다면 첨부해주세요"인 질문의 subtitle 초기화
UPDATE question q
INNER JOIN recruit r ON q.recruit_id = r.id
SET q.subtitle = '전공, 교육 경험, 부트캠프 수료 등 관련 활동 경험 증빙 문서로 대체 가능합니다.'
WHERE r.semester_id = 4
  AND r.job_family = 'PD'
  AND q.title = '포트폴리오가 있으시다면 첨부해주세요';

