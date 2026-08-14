ALTER TABLE apply
    ADD COLUMN selection_result VARCHAR(50) NOT NULL DEFAULT 'UNDECIDED',
    ADD COLUMN waitlist_number INT NULL,
    ADD CONSTRAINT uk_apply_recruit_waitlist_number UNIQUE (recruit_id, waitlist_number);
