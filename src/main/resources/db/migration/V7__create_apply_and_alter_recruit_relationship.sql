-- application_form 테이블에서 member, recruit 테이블에 대한 외래키 제약조건 제거
ALTER TABLE application_form DROP FOREIGN KEY FK7jm1xris1t3nyf2dc224preli;
ALTER TABLE application_form DROP FOREIGN KEY FKclurj75mp69f2y3m05bhs1ad6;

-- application_form 테이블에서 member_id, recruit_id 컬럼 제거
ALTER TABLE application_form DROP COLUMN member_id;
ALTER TABLE application_form DROP COLUMN recruit_id;

-- apply 테이블 생성
CREATE TABLE IF NOT EXISTS apply
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime(6) NULL,
    updated_at datetime(6) NULL,
    member_id  BIGINT      NOT NULL,
    recruit_id BIGINT      NOT NULL,
    status     VARCHAR(50) NOT NULL,
    CONSTRAINT pk_apply PRIMARY KEY (id),
    CONSTRAINT fk_apply_member FOREIGN KEY (member_id) REFERENCES member (id) ON DELETE NO ACTION,
    CONSTRAINT fk_apply_recruit FOREIGN KEY (recruit_id) REFERENCES recruit (id) ON DELETE NO ACTION
    ) ENGINE = InnoDB;

-- application_form 테이블에 apply_id 컬럼 추가 및 외래키 설정
ALTER TABLE application_form
    ADD COLUMN apply_id BIGINT NOT NULL,
    ADD CONSTRAINT fk_application_form_apply FOREIGN KEY (apply_id) REFERENCES apply (id) ON DELETE NO ACTION;

-- recruit 테이블에 semester_id 컬럼 추가 및 외래키 설정
ALTER TABLE recruit ADD CONSTRAINT fk_recruit_semester FOREIGN KEY (semester_id) REFERENCES semester (id) ON DELETE NO ACTION;