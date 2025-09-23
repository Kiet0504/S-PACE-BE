
CREATE TABLE company (
    company_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    status VARCHAR(255) NOT NULL
);

CREATE TABLE plan (
    plan_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID,
    plan_name VARCHAR(255),
    plan_type VARCHAR(255),
    max_employee INTEGER,
    max_event_per_month INTEGER,
    max_collaborators INTEGER,
    price DECIMAL(10,2),
    duration_month INTEGER,
    features TEXT,
    status VARCHAR(255),
    CONSTRAINT fk_plan_company FOREIGN KEY (company_id) REFERENCES company(company_id)
);

CREATE TABLE payment (
    payment_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id UUID NOT NULL,
    paid_at TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_payment_plan FOREIGN KEY (plan_id) REFERENCES plan(plan_id)
);

CREATE TABLE role (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE event (
    event_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    picture VARCHAR(255),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_event_company FOREIGN KEY (company_id) REFERENCES company(company_id)
);

CREATE TABLE team (
    team_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    team_name VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_event FOREIGN KEY (event_id) REFERENCES event(event_id)
);

CREATE TABLE "user" (
    user_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id INTEGER NOT NULL,
    team_id UUID,
    company_id UUID,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar VARCHAR(255),
    phone VARCHAR(15),
    address VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role(role_id),
    CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES company(company_id),
    CONSTRAINT fk_user_team FOREIGN KEY (team_id) REFERENCES team(team_id)
);

CREATE TABLE attendance_logs (
    attendance_logs_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    check_in_time TIMESTAMP NOT NULL,
    check_out_time TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_attendance_event FOREIGN KEY (event_id) REFERENCES event(event_id),
    CONSTRAINT fk_attendance_user FOREIGN KEY (user_id) REFERENCES "user"(user_id)
);

CREATE TABLE certificates (
    certificates_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    certificate_data BYTEA,
    certificate_file_path VARCHAR(500),
    certificate_code VARCHAR(50) NOT NULL UNIQUE,
    issued_date DATE NOT NULL,
    issued_by VARCHAR(255),
    CONSTRAINT fk_certificate_event FOREIGN KEY (event_id) REFERENCES event(event_id),
    CONSTRAINT fk_certificate_user FOREIGN KEY (user_id) REFERENCES "user"(user_id)
);

CREATE TABLE event_registration (
    event_registration_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    user_id UUID NOT NULL,
    registration_date TIMESTAMP NOT NULL,
    registration_data TEXT,
    reviewed_by UUID,
    reviewed_at TIMESTAMP,
    review_notes TEXT,
    status VARCHAR(255) NOT NULL,
    participation_status VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_registration_event FOREIGN KEY (event_id) REFERENCES event(event_id),
    CONSTRAINT fk_registration_user FOREIGN KEY (user_id) REFERENCES "user"(user_id),
    CONSTRAINT fk_registration_reviewer FOREIGN KEY (reviewed_by) REFERENCES "user"(user_id)
);

CREATE TABLE email_notifications (
    email_notifications_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID,
    user_id UUID NOT NULL,
    event_registration_id UUID,
    email_type VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    content TEXT,
    sent_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_email_event FOREIGN KEY (event_id) REFERENCES event(event_id),
    CONSTRAINT fk_email_user FOREIGN KEY (user_id) REFERENCES "user"(user_id),
    CONSTRAINT fk_email_registration FOREIGN KEY (event_registration_id) REFERENCES event_registration(event_registration_id)
);

CREATE TABLE event_tasks (
    event_tasks_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL,
    assigned_to UUID,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    deadline TIMESTAMP NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_task_team FOREIGN KEY (team_id) REFERENCES team(team_id),
    CONSTRAINT fk_task_user FOREIGN KEY (assigned_to) REFERENCES "user"(user_id)
);

CREATE TABLE task_reports (
    task_reports_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    event_tasks_id UUID NOT NULL,
    progress DECIMAL(5, 2) NOT NULL,
    note TEXT,
    reported_at TIMESTAMP NOT NULL,
    user_id UUID,
    CONSTRAINT fk_report_task FOREIGN KEY (event_tasks_id) REFERENCES event_tasks(event_tasks_id),
    CONSTRAINT fk_report_user FOREIGN KEY (user_id) REFERENCES "user"(user_id)
);

CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_user_company ON "user"(company_id);
CREATE INDEX idx_event_company ON event(company_id);
CREATE INDEX idx_event_dates ON event(start_date, end_date);
CREATE INDEX idx_attendance_event ON attendance_logs(event_id);
CREATE INDEX idx_attendance_user ON attendance_logs(user_id);
CREATE INDEX idx_registration_event ON event_registration(event_id);
CREATE INDEX idx_registration_user ON event_registration(user_id);

INSERT INTO role (role_name, description) VALUES
    ('ADMIN', 'System Administrator'),
    ('COMPANY_ADMIN', 'Company Administrator'),
    ('EVENT_MANAGER', 'Event Manager'),
    ('TEAM_LEADER', 'Team Leader'),
    ('EMPLOYEE', 'Regular Employee'),
    ('PARTICIPANT', 'Event Participant');