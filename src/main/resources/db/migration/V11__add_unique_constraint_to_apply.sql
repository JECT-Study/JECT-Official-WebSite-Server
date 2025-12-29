ALTER TABLE apply
ADD CONSTRAINT uk_apply_member_recruit UNIQUE (member_id, recruit_id);
