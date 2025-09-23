-- Update existing roles to match new structure
UPDATE role SET role_name = 'COLLABORATOR', description = 'Collaborator role for external users' WHERE role_id = 1;
UPDATE role SET role_name = 'ORGANIZER', description = 'Event organizer with management privileges' WHERE role_id = 2;
UPDATE role SET role_name = 'EMPLOYEE', description = 'Employee with standard access' WHERE role_id = 3;
UPDATE role SET role_name = 'ADMIN', description = 'System administrator with full access' WHERE role_id = 4;

-- Delete unused roles from V1
DELETE FROM role WHERE role_id IN (5, 6);

-- Add example companies (matching V1 table structure)
INSERT INTO company (company_id, company_name, address, status) VALUES
    (gen_random_uuid(), 'TechCorp Solutions', '123 Tech Street, Silicon Valley, CA', 'ACTIVE'),
    (gen_random_uuid(), 'EventPro Inc', '456 Event Avenue, New York, NY', 'ACTIVE'),
    (gen_random_uuid(), 'SpaceTech Ltd', '789 Space Boulevard, Houston, TX', 'ACTIVE');

-- Add example events (matching V1 table structure)
INSERT INTO event (event_id, company_id, title, description, location, picture, start_date, end_date, created_at, status) VALUES
    (gen_random_uuid(), (SELECT company_id FROM company WHERE company_name = 'SpaceTech Ltd' LIMIT 1), 'Space Technology Conference 2024', 'Annual conference on space technology and innovation', 'Houston Convention Center, TX', 'https://example.com/space-conf.jpg', '2024-10-15', '2024-10-17', NOW(), 'ACTIVE'),
    (gen_random_uuid(), (SELECT company_id FROM company WHERE company_name = 'TechCorp Solutions' LIMIT 1), 'Tech Innovation Summit', 'Summit showcasing latest technology innovations', 'San Francisco, CA', 'https://example.com/tech-summit.jpg', '2024-11-20', '2024-11-22', NOW(), 'PLANNING'),
    (gen_random_uuid(), (SELECT company_id FROM company WHERE company_name = 'EventPro Inc' LIMIT 1), 'Event Management Workshop', 'Workshop on modern event management techniques', 'New York, NY', 'https://example.com/workshop.jpg', '2024-12-05', '2024-12-06', NOW(), 'ACTIVE');

-- Add example users
INSERT INTO "user" (user_id, role_id, team_id, company_id, full_name, email, password_hash, avatar, phone, address, created_at, status) VALUES
    (gen_random_uuid(), 4, NULL, NULL, 'John Admin', 'admin@space.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'https://example.com/avatar1.jpg', '+REDACTED_PASSWORD67890', '123 Admin Street, City, State', NOW(), 'ACTIVE'),
    (gen_random_uuid(), 2, NULL, (SELECT company_id FROM company WHERE company_name = 'EventPro Inc' LIMIT 1), 'Jane Organizer', 'jane.organizer@eventpro.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'https://example.com/avatar2.jpg', '+REDACTED_PASSWORD67891', '456 Organizer Avenue, City, State', NOW(), 'ACTIVE'),
    (gen_random_uuid(), 3, NULL, (SELECT company_id FROM company WHERE company_name = 'TechCorp Solutions' LIMIT 1), 'Bob Employee', 'bob.employee@techcorp.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'https://example.com/avatar3.jpg', '+REDACTED_PASSWORD67892', '789 Employee Road, City, State', NOW(), 'ACTIVE'),
    (gen_random_uuid(), 1, NULL, (SELECT company_id FROM company WHERE company_name = 'SpaceTech Ltd' LIMIT 1), 'Alice Collaborator', 'alice.collaborator@spacetech.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'https://example.com/avatar4.jpg', '+REDACTED_PASSWORD67893', '321 Collaborator Lane, City, State', NOW(), 'ACTIVE');
