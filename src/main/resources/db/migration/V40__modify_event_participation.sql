ALTER TABLE event_participation
    DROP FOREIGN KEY FK_event_participation_member_activity;

ALTER TABLE event_participation
    DROP PRIMARY KEY,
    CHANGE COLUMN id member_activity_id BIGINT NOT NULL,
    ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST,
    ADD COLUMN participation_status VARCHAR(45) NULL AFTER semester_event_id;

UPDATE event_participation
SET participation_status = 'ATTENDED';

ALTER TABLE event_participation
    MODIFY COLUMN participation_status VARCHAR(45) NOT NULL,
    ADD CONSTRAINT UK_event_participation_activity_event UNIQUE (member_activity_id, semester_event_id),
    ADD CONSTRAINT FK_event_participation_member_activity
        FOREIGN KEY (member_activity_id) REFERENCES member_activity (id) ON DELETE CASCADE;
