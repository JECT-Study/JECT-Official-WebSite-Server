-- apply.status 컬럼은 VARCHAR(50) 타입이므로 DDL 변경 없이 REJECTED 값을 사용할 수 있음
-- 기존 허용 값: JOINED, TEMP_SAVED, SUBMITTED
-- 추가 허용 값: REJECTED (불합격 처리 시 지원서 내용 및 개인정보 파기 후 상태 전환)

