-- Add created_by column to track who created each event
-- This is needed for subscription limit validation
ALTER TABLE event ADD COLUMN created_by UUID;

-- Add foreign key constraint to user table
ALTER TABLE event ADD CONSTRAINT fk_event_created_by
    FOREIGN KEY (created_by) REFERENCES "user"(user_id);

-- Create index for better performance when querying events by creator
CREATE INDEX idx_event_created_by ON event(created_by);