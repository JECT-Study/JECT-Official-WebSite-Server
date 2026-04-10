CREATE TABLE IF NOT EXISTS mail_scenario
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime(6)           NULL,
    updated_at    datetime(6)           NULL,
    name          VARCHAR(100)          NOT NULL,
    category      VARCHAR(50)           NOT NULL,
    code          TEXT                  NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS mail_scenario_variables
(
    mail_scenario_id BIGINT      NOT NULL,
    variables        VARCHAR(50) NOT NULL,
    CONSTRAINT fk_mail_scenario_variables FOREIGN KEY (mail_scenario_id) REFERENCES mail_scenario (id)
) ENGINE = InnoDB;
