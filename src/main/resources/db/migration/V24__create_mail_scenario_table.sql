CREATE TABLE IF NOT EXISTS mail_scenario
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    name             VARCHAR(255)          NOT NULL,
    category         VARCHAR(50)           NOT NULL,
    type             VARCHAR(50)           NOT NULL DEFAULT 'ETC',
    scenario_code    VARCHAR(100)          NOT NULL,
    subject_template TEXT                  NOT NULL,
    body_template    TEXT                  NOT NULL,
    active           TINYINT(1)            NOT NULL DEFAULT 1,
    created_at       datetime(6)           NULL,
    updated_at       datetime(6)           NULL,
    CONSTRAINT PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE UNIQUE INDEX uk_mail_scenario_scenario_code ON mail_scenario (scenario_code);

CREATE TABLE IF NOT EXISTS mail_scenario_variables
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    mail_scenario_id BIGINT                NOT NULL,
    variable_key     VARCHAR(100)          NOT NULL,
    label            VARCHAR(50)           NOT NULL,
    input_type       VARCHAR(30)           NOT NULL,
    is_required      TINYINT(1)            NOT NULL DEFAULT 0,
    description      TEXT                  NULL,
    CONSTRAINT PRIMARY KEY (id),
    CONSTRAINT fk_mail_scenario_variables FOREIGN KEY (mail_scenario_id) REFERENCES mail_scenario (id) ON DELETE CASCADE
) ENGINE = InnoDB;
