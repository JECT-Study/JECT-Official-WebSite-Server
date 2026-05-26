-- 26-05-19 시점의 member 테이블 상태를 그대로 복사
-- 제약조건은 추후 수정, 추가
CREATE TABLE applicant
(
    id                bigint auto_increment primary key,
    created_at        datetime(6)                    null,
    updated_at        datetime(6)                    null,
    name              varchar(20)                    null,
    phone_number      varchar(12)                    null,
    email             varchar(30)                    not null,
    semester_id       bigint                         not null,
    job_family        varchar(45)                    null,
    role              varchar(10)                    not null,
    pin               varchar(255)                   null,
    status            varchar(45) default 'ACTIVE'   not null,
    is_deleted        tinyint(1)  default 0          not null,
    career_details    varchar(30)                    null,
    experience_period varchar(30)                    null,
    domain_name       varchar(50)                    null,
    region            varchar(30)                    null,
    member_type       varchar(45) default 'SEMESTER' not null
);

INSERT INTO applicant (id, created_at, updated_at, name, phone_number, email, semester_id, job_family, role, pin, status, is_deleted, career_details, experience_period, domain_name, region, member_type)
SELECT                 id, created_at, updated_at, name, phone_number, email, semester_id, job_family, role, pin, status, is_deleted, career_details, experience_period, domain_name, region, member_type
FROM member;