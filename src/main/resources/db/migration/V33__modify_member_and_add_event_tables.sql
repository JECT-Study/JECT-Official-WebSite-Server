-- 1. member_activity에 recruit_type_detail 컬럼 추가
ALTER TABLE member_activity
    ADD COLUMN recruit_type_detail VARCHAR(45) NULL;

UPDATE member_activity
SET recruit_type_detail = '정규모집';

-- 2. member_activity -> member 외래키 제거
ALTER TABLE member_activity
    DROP FOREIGN KEY FK_member_activity_member;

-- 3. member 테이블 불필요 컬럼 삭제
ALTER TABLE member
    DROP COLUMN semester_id,
    DROP COLUMN job_family,
    DROP COLUMN `role`,
    DROP COLUMN pin,
    DROP COLUMN status,
    DROP COLUMN career_details,
    DROP COLUMN experience_period,
    DROP COLUMN region,
    DROP COLUMN member_type;

-- 4. 기수 이벤트 정의 테이블 생성
CREATE TABLE IF NOT EXISTS semester_event
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  datetime(6)  NULL,
    updated_at  datetime(6)  NULL,
    semester_id BIGINT       NOT NULL,
    type        VARCHAR(45)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    is_required TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT PRIMARY KEY (id),
    CONSTRAINT FK_semester_event_semester FOREIGN KEY (semester_id) REFERENCES semester (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

-- 5. 이벤트 참여 기록 테이블 생성 (id = member_activity.id 공유)
CREATE TABLE IF NOT EXISTS event_participation
(
    id                BIGINT NOT NULL,
    created_at        datetime(6) NULL,
    updated_at        datetime(6) NULL,
    semester_event_id BIGINT NOT NULL,
    CONSTRAINT PRIMARY KEY (id),
    CONSTRAINT FK_event_participation_member_activity FOREIGN KEY (id) REFERENCES member_activity (id) ON DELETE CASCADE,
    CONSTRAINT FK_event_participation_semester_event FOREIGN KEY (semester_event_id) REFERENCES semester_event (id) ON DELETE NO ACTION
) ENGINE = InnoDB;
