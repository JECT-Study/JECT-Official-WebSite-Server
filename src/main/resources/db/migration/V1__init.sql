CREATE TABLE IF NOT EXISTS semester
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    semester_name VARCHAR(20) NOT NULL,
    is_recruiting TINYINT(1) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS member
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   datetime NULL,
    updated_at   datetime NULL,
    name         VARCHAR(20) NULL,
    phone_number VARCHAR(12) NULL,
    email        VARCHAR(30) NOT NULL,
    semester_id  BIGINT NOT NULL,
    job_family   VARCHAR(45) NULL,
    `role`       VARCHAR(10) NOT NULL,
    pin          VARCHAR(255) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    UNIQUE KEY UKmbmcqelty0fbrvxp1q58dn57t (email)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS team
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    name       VARCHAR(30) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS recruit
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  datetime NULL,
    updated_at  datetime NULL,
    semester_id BIGINT      NOT NULL,
    start_date  datetime    NOT NULL,
    end_date    datetime    NOT NULL,
    job_family  VARCHAR(45) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT uq_recruit_semester_job_family UNIQUE (semester_id, job_family)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS application_form
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    content    MEDIUMTEXT NULL,
    member_id  BIGINT NOT NULL,
    recruit_id BIGINT NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT FK7jm1xris1t3nyf2dc224preli FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE NO ACTION,
    CONSTRAINT FKclurj75mp69f2y3m05bhs1ad6 FOREIGN KEY (recruit_id) REFERENCES recruit (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS portfolio
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    created_at          datetime NULL,
    updated_at          datetime NULL,
    file_url            VARCHAR(2083) NOT NULL,
    file_name           VARCHAR(255)  NOT NULL,
    file_size           BIGINT NOT NULL,
    sequence            INT NOT NULL,
    application_form_id BIGINT NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT FKq0c0ebwomte7kc4pu3n2wuh9y FOREIGN KEY (application_form_id) REFERENCES application_form (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS project
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    name          VARCHAR(100) NULL,
    category      VARCHAR(30)  NOT NULL,
    semester_id   BIGINT       NOT NULL,
    summary       VARCHAR(100) NOT NULL,
    tech_stack    VARCHAR(255) NULL,
    start_date    date NULL,
    end_date      date NULL,
    `description` TEXT NULL,
    thumbnail_url VARCHAR(2083) NULL,
    service_url   VARCHAR(2083) NULL,
    team_id       BIGINT       NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT FK99hcloicqmg95ty11qht49n8x FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS project_intro
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    image_url  VARCHAR(2083) NOT NULL,
    category   VARCHAR(30)   NOT NULL,
    sequence   INT NULL,
    project_id BIGINT        NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT FKb7th9y61sopfua61igph7so8l FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS question
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime NULL,
    updated_at      datetime NULL,
    sequence        INT          NOT NULL,
    input_type      VARCHAR(10)  NOT NULL,
    is_required     TINYINT(1)            NOT NULL,
    title           VARCHAR(100) NOT NULL,
    label           VARCHAR(255) NULL,
    select_options  VARCHAR(255) NULL,
    input_hint      VARCHAR(255) NULL,
    max_text_length INT NULL,
    max_file_size   INT NULL,
    recruit_id      BIGINT       NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT FK3nnqj75r50htvoq9ykb421uo1 FOREIGN KEY (recruit_id) REFERENCES recruit (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS team_member
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    member_id  BIGINT NOT NULL,
    team_id    BIGINT NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT FKt5k957ydx0vngjtsljbelmu75 FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE NO ACTION,
    CONSTRAINT FK9ubp79ei4tv4crd0r9n7u5i6e FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE NO ACTION
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS email_send_group
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    code          VARCHAR(30)  NOT NULL,
    `description` VARCHAR(100) NOT NULL,
    template_name VARCHAR(100) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id),
    CONSTRAINT code_UNIQUE UNIQUE (code)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS jectalk
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  datetime NULL,
    updated_at  datetime NULL,
    name        VARCHAR(50)  NOT NULL,
    summary     VARCHAR(255) NOT NULL,
    youtube_url VARCHAR(2083) NULL,
    image_url   VARCHAR(2083) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS mini_study
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    name       VARCHAR(50)  NOT NULL,
    summary    VARCHAR(255) NOT NULL,
    link_url   VARCHAR(2083) NULL,
    image_url  VARCHAR(2083) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS review
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    link_url      VARCHAR(2083) NULL,
    title         VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    summary       VARCHAR(255) NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) ENGINE = InnoDB;