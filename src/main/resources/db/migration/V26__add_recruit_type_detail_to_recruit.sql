ALTER TABLE recruit
    ADD COLUMN recruit_type_detail VARCHAR(45) NULL;

UPDATE recruit
SET recruit_type_detail = CASE
    WHEN recruit_type = 'BACKFILL' THEN 'REFILL'
    ELSE 'REGULAR'
END
WHERE recruit_type_detail IS NULL;

ALTER TABLE recruit
    MODIFY COLUMN recruit_type_detail VARCHAR(45) NOT NULL;
