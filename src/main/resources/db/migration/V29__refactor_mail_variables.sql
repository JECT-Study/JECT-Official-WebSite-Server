TRUNCATE TABLE mail_scenario_variables;

ALTER TABLE mail_scenario_variables
    DROP COLUMN variable,
    ADD COLUMN variable_key VARCHAR(100) NOT NULL,
    ADD COLUMN label VARCHAR(50) NOT NULL,
    ADD COLUMN input_type VARCHAR(30) NOT NULL,
    ADD COLUMN is_required TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN description TEXT NULL;
