-- 멤버 활동 테이블 생성
CREATE TABLE IF NOT EXISTS member_activity
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    created_at        datetime(6) NULL,
    updated_at        datetime(6) NULL,
    member_id         BIGINT       NOT NULL,
    is_deleted        TINYINT(1)   NOT NULL DEFAULT 0,
    career_details    VARCHAR(30)  NULL,
    member_type       VARCHAR(45)  NOT NULL,
    job_family        VARCHAR(45)  NULL,
    activity_status   VARCHAR(45)  NOT NULL DEFAULT 'ACTIVE',
    experience_period VARCHAR(30)  NULL,
    start_date        date NULL,
    end_date          date NULL,
    memo              VARCHAR(100) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT FK_member_activity_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

-- 아래 관리 테이블은 부모인 member_activity.id를 pk로 사용
-- 기존 semester 테이블과 구별하기 위해 prefix를 테이블명에 추가

-- 일반 구성원 관리 테이블
CREATE TABLE IF NOT EXISTS member_semester
(
    id            BIGINT      NOT NULL,
    created_at    datetime(6) NULL,
    updated_at    datetime(6) NULL,
    semester_id   BIGINT      NOT NULL,
    team_id       BIGINT NULL,
    cert_number   VARCHAR(20) NULL,
    first_review  VARCHAR(255) NULL,
    second_review VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT FK_member_semester_member_activity FOREIGN KEY (id) REFERENCES member_activity (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- 메이커스 관리 테이블
CREATE TABLE IF NOT EXISTS member_makers
(
    id                                  BIGINT      NOT NULL,
    created_at                          datetime(6) NULL,
    updated_at                          datetime(6) NULL,
    team_name                           VARCHAR(20) NULL,
    mentoring_availability             VARCHAR(30) NULL,
    project_supplement_availability    VARCHAR(30) NULL,
    speaker_availability               VARCHAR(30) NULL,
    career_level                       VARCHAR(30) NULL,
    skills                             VARCHAR(255) NULL,
    company                            VARCHAR(30) NULL,
    expert_topics                      VARCHAR(30) NULL,
    activity_cert_number               VARCHAR(20) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT FK_member_makers_member_activity FOREIGN KEY (id) REFERENCES member_activity (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- 서포터즈 관리 테이블
CREATE TABLE IF NOT EXISTS member_supporters
(
    id                   BIGINT NOT NULL,
    created_at           datetime(6) NULL,
    updated_at           datetime(6) NULL,
    activity_cert_number VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT FK_member_supporters_member_activity FOREIGN KEY (id) REFERENCES member_activity (id) ON DELETE CASCADE
) ENGINE = InnoDB;
