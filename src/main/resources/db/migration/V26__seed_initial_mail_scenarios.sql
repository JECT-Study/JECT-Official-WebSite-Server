-- 1. 충원 모집 - 불합격 통지
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('충원 모집 - 불합격 통지', 'RECRUIT_ADDITIONAL', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'RECRUIT_ALERT_APPLY_URL'), (@scenario_id, 'NAME');

-- 2. 일반 구성원 - 최종 합격 통지
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('일반 구성원 - 최종 합격 통지', 'MEMBER', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'JOIN_PROCESS_URL'), (@scenario_id, 'EVENT_DATE_TIME'), (@scenario_id, 'EVENT_LOCATION'), (@scenario_id, 'NAME'), (@scenario_id, 'GENERATION');

-- 3. 일반 구성원 - 예비 합격 통지
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('일반 구성원 - 예비 합격 통지', 'MEMBER', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'NAME');

-- 4. 일반 구성원 - 불합격 통지
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('일반 구성원 - 불합격 통지', 'MEMBER', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'RECRUIT_ALERT_APPLY_URL'), (@scenario_id, 'NAME');

-- 5. 일반 구성원 - n기 모집 시작 알림
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('일반 구성원 - n기 모집 시작 알림', 'MEMBER', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'JECT_OFFICIAL_SITE_URL'), (@scenario_id, 'GENERATION');

-- 6. 메이커스 팀 - 1차 합격 통지
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('메이커스 팀 - 1차 합격 통지', 'MAKERS', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'COFFEE_CHAT_RESERVATION_URL'), (@scenario_id, 'NAME');

-- 7. 메이커스 팀 - 1차 불합격 통지
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('메이커스 팀 - 1차 불합격 통지', 'MAKERS', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'RECRUIT_ALERT_APPLY_URL'), (@scenario_id, 'NAME');

-- 8. 메이커스 팀 - 최종 합격 통지
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('메이커스 팀 - 최종 합격 통지', 'MAKERS', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'MAKERS_N_TEAM_JOIN_PROCESS_URL'), (@scenario_id, 'MAKERS_N_TEAM_ACTIVITY_NOTICE_URL'), (@scenario_id, 'NAME');

-- 9. 메이커스 팀 - 최종 불합격 통지
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('메이커스 팀 - 최종 불합격 통지', 'MAKERS', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'RECRUIT_ALERT_APPLY_URL'), (@scenario_id, 'NAME');

-- 10. 관리자 계정 접속 정보 안내
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('관리자 계정 접속 정보 안내', 'ADMIN_SUPPORT', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'NAME');

-- 11. 비밀번호 초기화 안내
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('비밀번호 초기화 안내', 'ADMIN_SUPPORT', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'NAME');

-- 12. 문의 사항 답변 알림
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('문의 사항 답변 알림', 'ADMIN_SUPPORT', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'NAME');

-- 13. 기타 공지 사항
INSERT INTO mail_scenario (name, category, code, created_at, updated_at) VALUES ('기타 공지 사항', 'GENERAL', '', NOW(), NOW());
SET @scenario_id = LAST_INSERT_ID();
INSERT INTO mail_scenario_variables (mail_scenario_id, variables) VALUES (@scenario_id, 'NAME');
