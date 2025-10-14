-- Update event picture from example URL to actual uploaded image
-- Run this script in your PostgreSQL database

-- First, check current event data
SELECT event_id, title, picture
FROM event
WHERE title LIKE '%Space Technology%' OR title LIKE '%Conference%';

-- Update the event picture to use the actual uploaded image
UPDATE event
SET picture = 'http://localhost:8080/uploads/ce844772-f099-47f7-8b58-9b80b2b2602a.jpg'
WHERE title = 'Space Technology Conference 2024';

-- Verify the update
SELECT event_id, title, picture
FROM event
WHERE title = 'Space Technology Conference 2024';
