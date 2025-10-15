-- V4: Add created_by field to event table
ALTER TABLE event ADD COLUMN created_by UUID;
ALTER TABLE event ADD CONSTRAINT fk_event_created_by FOREIGN KEY (created_by) REFERENCES "user"(user_id);

-- Update existing events to have a default creator (first admin user)
UPDATE event SET created_by = (SELECT user_id FROM "user" WHERE role_id = 1 LIMIT 1) WHERE created_by IS NULL;

-- Make the field NOT NULL after updating existing records
ALTER TABLE event ALTER COLUMN created_by SET NOT NULL;

