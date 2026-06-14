-- 1. member_activity에 recruit_type_detail 컬럼 추가
ALTER TABLE member_activity
    ADD COLUMN recruit_type_detail VARCHAR(45) NULL;

-- 기존 데이터 백필
UPDATE member_activity
SET recruit_type_detail = 'REGULAR';

-- NOT NULL 적용
ALTER TABLE member_activity
    MODIFY recruit_type_detail VARCHAR(45) NOT NULL;

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
    CONSTRAINT PRIMARY KEY (id)
) ENGINE = InnoDB;

-- 5. 이벤트 참여 기록 테이블 생성 (id = member_activity.id 공유)
CREATE TABLE IF NOT EXISTS event_participation
(
    id                BIGINT NOT NULL,
    created_at        datetime(6) NULL,
    updated_at        datetime(6) NULL,
    semester_event_id BIGINT NOT NULL,
    CONSTRAINT PRIMARY KEY (id),
    CONSTRAINT FK_event_participation_member_activity FOREIGN KEY (id) REFERENCES member_activity (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- 6. team_member 테이블 FK 및 테이블 삭제 (team 참조는 member_semester.team_id로 이전)
ALTER TABLE team_member
    DROP FOREIGN KEY FKt5k957ydx0vngjtsljbelmu75,
    DROP FOREIGN KEY FK9ubp79ei4tv4crd0r9n7u5i6e;

DROP TABLE IF EXISTS team_member;
