-- Add additional fields to event table
-- These fields are: max_participants, requirements, contact_info

-- Add max_participants column
ALTER TABLE event 
ADD COLUMN IF NOT EXISTS max_participants INTEGER;

-- Add requirements column
ALTER TABLE event 
ADD COLUMN IF NOT EXISTS requirements TEXT;

-- Add contact_info column
ALTER TABLE event 
ADD COLUMN IF NOT EXISTS contact_info VARCHAR(500);

-- Add comments for documentation
COMMENT ON COLUMN event.max_participants IS 'Maximum number of participants allowed for the event';
COMMENT ON COLUMN event.requirements IS 'Requirements for participants to join the event';
COMMENT ON COLUMN event.contact_info IS 'Contact information for the event';


