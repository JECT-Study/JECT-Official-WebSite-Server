CREATE TABLE IF NOT EXISTS semester
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    semester_name VARCHAR(20) NOT NULL,
    is_recruiting TINYINT(1) NOT NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS member
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   datetime NULL,
    updated_at   datetime NULL,
    phone_number VARCHAR(12) NULL,
    name         VARCHAR(20) NULL,
    email        VARCHAR(30) NOT NULL,
    pin          VARCHAR(255) NOT NULL,
    job_family   VARCHAR(45) NULL,
    `role`       VARCHAR(10) NOT NULL,
    semester_id  BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_email (email),
    CONSTRAINT fk_member_semester FOREIGN KEY (semester_id) REFERENCES semester(id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS team
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    name       VARCHAR(30) NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS recruit
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    semester_id BIGINT      NOT NULL,
    job_family  VARCHAR(45) NOT NULL,
    start_date  datetime    NOT NULL,
    end_date    datetime    NOT NULL,
    created_at  datetime NULL,
    updated_at  datetime NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_recruit_semester_job_family (semester_id, job_family),
    CONSTRAINT fk_recruit_semester FOREIGN KEY (semester_id) REFERENCES semester (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS application_form
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    member_id  BIGINT NOT NULL,
    recruit_id BIGINT NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    content    LONGTEXT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_application_form_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE NO ACTION,
    CONSTRAINT fk_application_form_recruit FOREIGN KEY (recruit_id) REFERENCES recruit(id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS portfolio
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    application_form_id BIGINT NOT NULL,
    file_size           BIGINT NOT NULL,
    file_url            VARCHAR(2083) NOT NULL,
    file_name           VARCHAR(255)  NOT NULL,
    sequence            INT NOT NULL,
    created_at          datetime NULL,
    updated_at          datetime NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_portfolio_application FOREIGN KEY (application_form_id) REFERENCES application_form (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS project
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    team_id       BIGINT       NOT NULL,
    semester_id   BIGINT       NOT NULL,
    start_date    date NULL,
    end_date      date NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    name          VARCHAR(100) NULL,
    summary       VARCHAR(100) NOT NULL,
    service_url   VARCHAR(2083) NULL,
    thumbnail_url VARCHAR(2083) NULL,
    `description` LONGTEXT NULL,
    tech_stack    VARCHAR(255) NULL,
    category      VARCHAR(30)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_project_semester FOREIGN KEY (semester_id) REFERENCES semester (id) ON DELETE NO ACTION,
    CONSTRAINT fk_project_team FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS project_intro
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    project_id BIGINT        NOT NULL,
    sequence   INT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    image_url  VARCHAR(2083) NOT NULL,
    category   VARCHAR(30)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_project_intro_project FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS question
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    recruit_id      BIGINT       NOT NULL,
    created_at      datetime NULL,
    updated_at      datetime NULL,
    title           VARCHAR(100) NOT NULL,
    label           VARCHAR(255) NULL,
    input_hint      VARCHAR(255) NULL,
    input_type      VARCHAR(10)  NOT NULL,
    is_required     TINYINT(1)            NOT NULL,
    max_text_length INT NULL,
    sequence        INT          NOT NULL,
    max_file_size   INT NULL,
    select_options  VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_question_recruit FOREIGN KEY (recruit_id) REFERENCES recruit (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS team_member
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    member_id  BIGINT NOT NULL,
    team_id    BIGINT NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_team_member_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE NO ACTION,
    CONSTRAINT fk_team_member_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS email_send_group
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    code          VARCHAR(30)  NOT NULL,
    `description` VARCHAR(100) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_email_send_group_code (code)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS jectalk
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    image_url   VARCHAR(2083) NULL,
    youtube_url VARCHAR(2083) NULL,
    summary     VARCHAR(255) NOT NULL,
    created_at  datetime NULL,
    updated_at  datetime NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS mini_study
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    image_url  VARCHAR(2083) NULL,
    link_url   VARCHAR(2083) NULL,
    summary    VARCHAR(255) NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS review
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    title         VARCHAR(255) NOT NULL,
    summary       VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    link_url      VARCHAR(2083) NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;