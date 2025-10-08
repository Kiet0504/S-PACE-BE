-- Minimal Sample Data for EventTasks Testing
-- This script creates only the essential data needed to test EventTasks functionality
-- Safe to run without schema conflicts

-- Insert minimal company data if not exists
INSERT INTO company (company_id, company_name, address, status)
SELECT '550e8400-e29b-41d4-a716-446655440001', 'TechCorp Vietnam', '123 Nguyen Van Cu, District 1, Ho Chi Minh City', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM company WHERE company_id = '550e8400-e29b-41d4-a716-446655440001');

-- Insert minimal event data if not exists
INSERT INTO event (event_id, company_id, title, description, location, start_date, end_date, created_at, status)
SELECT '880e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'SAIGON TALK 9 | PERSONALIZE: BE REAL TRƯỚC, BE TECH SAU', 'Hội thảo công nghệ lớn nhất năm 2024', 'Saigon Exhibition and Convention Center', '2024-08-15', '2024-08-17', '2024-07-01 09:00:00', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM event WHERE event_id = '880e8400-e29b-41d4-a716-446655440001');

-- Insert teams
INSERT INTO team (team_id, event_id, team_name, quantity, created_at) VALUES
    ('990e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440001', 'Team Media', 8, '2024-07-02 09:00:00'),
    ('990e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440001', 'Team Event', 12, '2024-07-02 09:30:00'),
    ('990e8400-e29b-41d4-a716-446655440003', '880e8400-e29b-41d4-a716-446655440001', 'Team Planning', 6, '2024-07-02 10:00:00'),
    ('990e8400-e29b-41d4-a716-446655440004', '880e8400-e29b-41d4-a716-446655440001', 'Team External Relation', 10, '2024-07-02 10:30:00')
ON CONFLICT (team_id) DO NOTHING;

-- Insert users (using role IDs from V2: 1=COLLABORATOR, 2=ORGANIZER, 3=EMPLOYEE, 4=ADMIN)
INSERT INTO "user" (user_id, role_id, team_id, company_id, full_name, email, password_hash, phone, address, created_at, status) VALUES
    ('aa0e8400-e29b-41d4-a716-446655440008', 1, '990e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Nguyễn Văn Photo', 'photo@techcorp.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '090REDACTED_PASSWORD74', '258 Photo Lane, HCMC', '2024-07-03 09:00:00', 'ACTIVE'),
    ('aa0e8400-e29b-41d4-a716-446655440009', 1, '990e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'Trần Thị Video', 'video@techcorp.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '090REDACTED_PASSWORD75', '369 Video Ave, HCMC', '2024-07-03 09:30:00', 'ACTIVE'),
    ('aa0e8400-e29b-41d4-a716-446655440010', 1, '990e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'Lê Minh Setup', 'setup@techcorp.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '090REDACTED_PASSWORD76', '741 Setup Road, HCMC', '2024-07-03 10:00:00', 'ACTIVE'),
    ('aa0e8400-e29b-41d4-a716-446655440011', 1, '990e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'Phạm Văn Sound', 'sound@techcorp.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '090REDACTED_PASSWORD77', '852 Sound Street, HCMC', '2024-07-03 10:30:00', 'ACTIVE'),
    ('aa0e8400-e29b-41d4-a716-446655440012', 1, '990e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440001', 'Võ Thị Schedule', 'schedule@techcorp.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '090REDACTED_PASSWORD78', '963 Schedule Plaza, HCMC', '2024-07-03 11:00:00', 'ACTIVE'),
    ('aa0e8400-e29b-41d4-a716-446655440013', 1, '990e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', 'Đặng Văn PR', 'pr@techcorp.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '090REDACTED_PASSWORD79', '174 PR Lane, HCMC', '2024-07-03 11:30:00', 'ACTIVE'),
    ('aa0e8400-e29b-41d4-a716-446655440014', 1, '990e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440001', 'Hoàng Thị Contact', 'contact@techcorp.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '090REDACTED_PASSWORD80', '285 Contact Ave, HCMC', '2024-07-03 12:00:00', 'ACTIVE')
ON CONFLICT (user_id) DO NOTHING;

-- Clear existing event_tasks data
DELETE FROM task_reports WHERE event_tasks_id IN (SELECT event_tasks_id FROM event_tasks);
DELETE FROM event_tasks;

-- Insert comprehensive EventTasks sample data
INSERT INTO event_tasks (event_tasks_id, team_id, assigned_to, title, description, deadline, status, created_at, updated_at) VALUES
    -- Team Media Tasks (Team Media = team-1 in frontend mapping)
    ('bb0e8400-e29b-41d4-a716-446655440001', '990e8400-e29b-41d4-a716-446655440001', 'aa0e8400-e29b-41d4-a716-446655440008', 'Chụp ảnh sự kiện chính', 'Chụp ảnh các diễn giả và khách mời trong ngày đầu tiên của sự kiện. Cần đảm bảo chất lượng cao và góc chụp đẹp.', '2024-08-15 18:00:00', 'TODO', '2024-07-05 09:00:00', '2024-07-05 09:00:00'),

    ('bb0e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440001', 'aa0e8400-e29b-41d4-a716-446655440009', 'Quay video highlight sự kiện', 'Quay video những khoảnh khắc nổi bật của sự kiện để làm video tóm tắt. Focus vào keynote speakers và audience reactions.', '2024-08-16 20:00:00', 'TODO', '2024-07-05 09:30:00', '2024-07-05 09:30:00'),

    ('bb0e8400-e29b-41d4-a716-446655440003', '990e8400-e29b-41d4-a716-446655440001', 'aa0e8400-e29b-41d4-a716-446655440008', 'Chỉnh sửa ảnh sự kiện', 'Post-processing ảnh chụp trong ngày đầu và chuẩn bị đăng lên social media. Color grading và cropping theo brand guidelines.', '2024-08-16 10:00:00', 'IN_PROGRESS', '2024-07-05 10:00:00', '2024-08-15 19:00:00'),

    ('bb0e8400-e29b-41d4-a716-446655440004', '990e8400-e29b-41d4-a716-446655440001', 'aa0e8400-e29b-41d4-a716-446655440009', 'Live streaming sự kiện', 'Phát trực tiếp sự kiện lên Facebook, YouTube và LinkedIn. Đảm bảo chất lượng audio/video ổn định.', '2024-08-15 08:00:00', 'COMPLETED', '2024-07-05 10:30:00', '2024-08-15 08:30:00'),

    ('bb0e8400-e29b-41d4-a716-446655440018', '990e8400-e29b-41d4-a716-446655440001', 'aa0e8400-e29b-41d4-a716-446655440008', 'Tạo content social media', 'Tạo nội dung đăng trên các kênh social media trong và sau sự kiện. Bao gồm stories, posts và reels.', '2024-08-17 22:00:00', 'TODO', '2024-07-05 11:00:00', '2024-07-05 11:00:00'),

    -- Team Event Tasks (Team Event = team-2 in frontend mapping)
    ('bb0e8400-e29b-41d4-a716-446655440005', '990e8400-e29b-41d4-a716-446655440002', 'aa0e8400-e29b-41d4-a716-446655440010', 'Setup sân khấu chính', 'Lắp đặt và trang trí sân khấu chính cho các buổi thuyết trình. Kiểm tra backdrop, lighting và stage layout.', '2024-08-14 18:00:00', 'COMPLETED', '2024-07-05 11:00:00', '2024-08-14 17:30:00'),

    ('bb0e8400-e29b-41d4-a716-446655440006', '990e8400-e29b-41d4-a716-446655440002', 'aa0e8400-e29b-41d4-a716-446655440011', 'Test âm thanh và ánh sáng', 'Kiểm tra và điều chỉnh hệ thống âm thanh, ánh sáng trước sự kiện. Sound check với tất cả microphones.', '2024-08-14 20:00:00', 'COMPLETED', '2024-07-05 11:30:00', '2024-08-14 19:45:00'),

    ('bb0e8400-e29b-41d4-a716-446655440007', '990e8400-e29b-41d4-a716-446655440002', 'aa0e8400-e29b-41d4-a716-446655440010', 'Setup booth triển lãm', 'Thiết lập các booth cho các công ty tài trợ và đối tác. Đảm bảo branding và vị trí theo floor plan.', '2024-08-15 07:00:00', 'IN_PROGRESS', '2024-07-05 12:00:00', '2024-08-14 21:00:00'),

    ('bb0e8400-e29b-41d4-a716-446655440008', '990e8400-e29b-41d4-a716-446655440002', 'aa0e8400-e29b-41d4-a716-446655440011', 'Vận hành âm thanh trong sự kiện', 'Điều khiển âm thanh và hỗ trợ kỹ thuật trong suốt sự kiện. Monitoring audio levels và quick fixes.', '2024-08-17 18:00:00', 'TODO', '2024-07-05 12:30:00', '2024-07-05 12:30:00'),

    ('bb0e8400-e29b-41d4-a716-446655440019', '990e8400-e29b-41d4-a716-446655440002', 'aa0e8400-e29b-41d4-a716-446655440010', 'Chuẩn bị khu vực networking', 'Setup không gian networking và coffee break. Bao gồm bàn ghế, decoration và catering coordination.', '2024-08-15 06:00:00', 'TODO', '2024-07-05 13:00:00', '2024-07-05 13:00:00'),

    -- Team Planning Tasks (Team Planning = team-3 in frontend mapping)
    ('bb0e8400-e29b-41d4-a716-446655440009', '990e8400-e29b-41d4-a716-446655440003', 'aa0e8400-e29b-41d4-a716-446655440012', 'Lên lịch chi tiết sự kiện', 'Tạo timeline chi tiết cho tất cả hoạt động trong 3 ngày sự kiện. Coordinate với tất cả teams.', '2024-08-10 17:00:00', 'COMPLETED', '2024-07-05 13:00:00', '2024-08-09 16:30:00'),

    ('bb0e8400-e29b-41d4-a716-446655440010', '990e8400-e29b-41d4-a716-446655440003', 'aa0e8400-e29b-41d4-a716-446655440012', 'Phối hợp với diễn giả', 'Liên lạc và xác nhận lịch trình với tất cả diễn giả. Brief về technical requirements và schedule.', '2024-08-12 12:00:00', 'COMPLETED', '2024-07-05 13:30:00', '2024-08-11 15:00:00'),

    ('bb0e8400-e29b-41d4-a716-446655440011', '990e8400-e29b-41d4-a716-446655440003', 'aa0e8400-e29b-41d4-a716-446655440012', 'Chuẩn bị backup plan', 'Lên kế hoạch dự phòng cho các tình huống bất ngờ có thể xảy ra. Weather, technical issues, speaker cancellations.', '2024-08-13 17:00:00', 'IN_PROGRESS', '2024-07-05 14:00:00', '2024-08-12 10:00:00'),

    ('bb0e8400-e29b-41d4-a716-446655440020', '990e8400-e29b-41d4-a716-446655440003', 'aa0e8400-e29b-41d4-a716-446655440012', 'Quản lý logistics', 'Điều phối logistics cho venue, catering, transportation cho speakers và VIP guests.', '2024-08-14 12:00:00', 'TODO', '2024-07-05 14:30:00', '2024-07-05 14:30:00'),

    -- Team External Relation Tasks (Team External Relation = team-4 in frontend mapping)
    ('bb0e8400-e29b-41d4-a716-446655440012', '990e8400-e29b-41d4-a716-446655440004', 'aa0e8400-e29b-41d4-a716-446655440013', 'Liên lạc với báo chí', 'Gửi thông cáo báo chí và mời các tờ báo đưa tin về sự kiện. Tạo press kit và coordinate interviews.', '2024-08-12 17:00:00', 'COMPLETED', '2024-07-05 14:30:00', '2024-08-11 16:45:00'),

    ('bb0e8400-e29b-41d4-a716-446655440013', '990e8400-e29b-41d4-a716-446655440004', 'aa0e8400-e29b-41d4-a716-446655440014', 'Quản lý quan hệ đối tác', 'Duy trì liên lạc và hỗ trợ các đối tác tài trợ trong sự kiện. Đảm bảo sponsor benefits delivery.', '2024-08-17 20:00:00', 'TODO', '2024-07-05 15:00:00', '2024-07-05 15:00:00'),

    ('bb0e8400-e29b-41d4-a716-446655440014', '990e8400-e29b-41d4-a716-446655440004', 'aa0e8400-e29b-41d4-a716-446655440013', 'Tổ chức họp báo', 'Chuẩn bị và tổ chức buổi họp báo trước sự kiện. Đã bị hủy do thay đổi strategy.', '2024-08-13 14:00:00', 'CANCELLED', '2024-07-05 15:30:00', '2024-08-10 09:00:00'),

    ('bb0e8400-e29b-41d4-a716-446655440015', '990e8400-e29b-41d4-a716-446655440004', 'aa0e8400-e29b-41d4-a716-446655440014', 'Networking với khách VIP', 'Tổ chức các hoạt động networking cho khách VIP và diễn giả. Private dinner và exclusive sessions.', '2024-08-16 19:00:00', 'TODO', '2024-07-05 16:00:00', '2024-07-05 16:00:00'),

    ('bb0e8400-e29b-41d4-a716-446655440021', '990e8400-e29b-41d4-a716-446655440004', 'aa0e8400-e29b-41d4-a716-446655440013', 'Quản lý attendee check-in', 'Coordinate quá trình check-in của attendees và speakers. Setup registration desk và welcome kits.', '2024-08-15 07:30:00', 'TODO', '2024-07-05 16:30:00', '2024-07-05 16:30:00'),

    -- Additional mixed tasks to test filtering
    ('bb0e8400-e29b-41d4-a716-446655440022', '990e8400-e29b-41d4-a716-446655440001', NULL, 'Backup nhiếp ảnh', 'Photographer dự phòng cho trường hợp emergency. Standby với full equipment.', '2024-08-16 12:00:00', 'TODO', '2024-07-06 09:00:00', '2024-07-06 09:00:00'),

    ('bb0e8400-e29b-41d4-a716-446655440023', '990e8400-e29b-41d4-a716-446655440002', NULL, 'Kiểm tra thiết bị AV', 'Final check tất cả audio visual equipment trước giờ G. Test projectors, mics, screens.', '2024-08-15 06:30:00', 'IN_PROGRESS', '2024-07-06 10:00:00', '2024-08-14 20:00:00'),

    ('bb0e8400-e29b-41d4-a716-446655440024', '990e8400-e29b-41d4-a716-446655440003', NULL, 'Review checklist cuối cùng', 'Kiểm tra lại toàn bộ checklist và confirm readiness với tất cả team leads.', '2024-08-14 23:00:00', 'COMPLETED', '2024-07-06 11:00:00', '2024-08-14 22:30:00'),

    ('bb0e8400-e29b-41d4-a716-446655440025', '990e8400-e29b-41d4-a716-446655440004', NULL, 'Post-event follow up', 'Gửi thank you notes và collect feedback từ attendees, speakers và partners.', '2024-08-20 17:00:00', 'TODO', '2024-07-06 12:00:00', '2024-07-06 12:00:00');

COMMIT;

-- Show summary of EventTasks data with team mapping for frontend
SELECT
    CASE
        WHEN t.team_name = 'Team Media' THEN 'team-1'
        WHEN t.team_name = 'Team Event' THEN 'team-2'
        WHEN t.team_name = 'Team Planning' THEN 'team-3'
        WHEN t.team_name = 'Team External Relation' THEN 'team-4'
        ELSE t.team_id::text
    END as frontend_team_id,
    t.team_name,
    COUNT(et.*) as total_tasks,
    COUNT(CASE WHEN et.status = 'TODO' THEN 1 END) as todo_tasks,
    COUNT(CASE WHEN et.status = 'IN_PROGRESS' THEN 1 END) as in_progress_tasks,
    COUNT(CASE WHEN et.status = 'COMPLETED' THEN 1 END) as completed_tasks,
    COUNT(CASE WHEN et.status = 'CANCELLED' THEN 1 END) as cancelled_tasks
FROM team t
LEFT JOIN event_tasks et ON t.team_id = et.team_id
WHERE t.event_id = '880e8400-e29b-41d4-a716-446655440001'
GROUP BY t.team_id, t.team_name
ORDER BY t.team_name;

-- Show sample tasks for verification
SELECT
    et.title,
    t.team_name,
    u.full_name as assigned_to,
    et.status,
    et.deadline
FROM event_tasks et
JOIN team t ON et.team_id = t.team_id
LEFT JOIN "user" u ON et.assigned_to = u.user_id
ORDER BY t.team_name, et.created_at
LIMIT 10;