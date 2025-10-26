-- Tạo bảng điều kiện duyệt tự động cho công ty
CREATE TABLE company_auto_approval_rules (
    rule_id UUID NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(255) NOT NULL,
    rule_description TEXT,
    field_name VARCHAR(100) NOT NULL, -- Tên trường cần kiểm tra (company_name, address, etc.)
    field_type VARCHAR(50) NOT NULL, -- STRING, NUMBER, BOOLEAN, etc.
    operator VARCHAR(20) NOT NULL, -- EQUALS, CONTAINS, GREATER_THAN, LESS_THAN, etc.
    expected_value TEXT, -- Giá trị mong đợi
    is_active BOOLEAN DEFAULT true,
    priority INTEGER DEFAULT 0, -- Độ ưu tiên (số càng cao càng ưu tiên)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Thêm trường validation vào bảng company
ALTER TABLE company ADD COLUMN is_auto_approved BOOLEAN DEFAULT false;
ALTER TABLE company ADD COLUMN auto_approval_reason TEXT;
ALTER TABLE company ADD COLUMN validation_score INTEGER DEFAULT 0;
ALTER TABLE company ADD COLUMN validation_details JSONB;

-- Tạo index cho performance
CREATE INDEX idx_company_auto_approval_rules_active ON company_auto_approval_rules(is_active);
CREATE INDEX idx_company_auto_approval_rules_priority ON company_auto_approval_rules(priority DESC);
CREATE INDEX idx_company_validation_score ON company(validation_score);

-- Thêm dữ liệu mẫu cho các điều kiện duyệt tự động
INSERT INTO company_auto_approval_rules (rule_name, rule_description, field_name, field_type, operator, expected_value, priority) VALUES
('Company Name Length', 'Tên công ty phải có ít nhất 3 ký tự', 'company_name', 'STRING', 'LENGTH_GREATER_THAN', '2', 10),
('Company Name Not Empty', 'Tên công ty không được để trống', 'company_name', 'STRING', 'NOT_EMPTY', '', 20),
('Address Provided', 'Có địa chỉ công ty', 'address', 'STRING', 'NOT_EMPTY', '', 5),
('Company Name Format', 'Tên công ty không chứa ký tự đặc biệt', 'company_name', 'STRING', 'REGEX_MATCH', '^[a-zA-Z0-9\\s\\u00C0-\\u1EF9]+$', 15);

-- Tạo function để tự động tính điểm validation
CREATE OR REPLACE FUNCTION calculate_company_validation_score(p_company_id UUID)
RETURNS INTEGER AS $$
DECLARE
    total_score INTEGER := 0;
    rule_record RECORD;
    company_record RECORD;
    field_value TEXT;
    is_valid BOOLEAN;
BEGIN
    -- Lấy thông tin công ty
    SELECT * INTO company_record FROM company WHERE company.company_id = p_company_id;
    
    IF NOT FOUND THEN
        RETURN 0;
    END IF;
    
    -- Duyệt qua tất cả các rules đang active
    FOR rule_record IN 
        SELECT * FROM company_auto_approval_rules 
        WHERE is_active = true 
        ORDER BY priority DESC
    LOOP
        -- Lấy giá trị field cần kiểm tra
        CASE rule_record.field_name
            WHEN 'company_name' THEN field_value := company_record.company_name;
            WHEN 'address' THEN field_value := company_record.address;
            ELSE field_value := '';
        END CASE;
        
        -- Kiểm tra điều kiện
        is_valid := false;
        
        CASE rule_record.operator
            WHEN 'NOT_EMPTY' THEN
                is_valid := (field_value IS NOT NULL AND field_value != '');
            WHEN 'LENGTH_GREATER_THAN' THEN
                is_valid := (LENGTH(field_value) > rule_record.expected_value::INTEGER);
            WHEN 'REGEX_MATCH' THEN
                is_valid := (field_value ~ rule_record.expected_value);
            WHEN 'EQUALS' THEN
                is_valid := (field_value = rule_record.expected_value);
            WHEN 'CONTAINS' THEN
                is_valid := (field_value ILIKE '%' || rule_record.expected_value || '%');
            ELSE
                is_valid := false;
        END CASE;
        
        -- Cộng điểm nếu điều kiện đúng
        IF is_valid THEN
            total_score := total_score + rule_record.priority;
        END IF;
    END LOOP;
    
    RETURN total_score;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger để tự động cập nhật validation score khi company được tạo/cập nhật
CREATE OR REPLACE FUNCTION update_company_validation_score()
RETURNS TRIGGER AS $$
DECLARE
    new_score INTEGER;
    approval_threshold INTEGER := 30; -- Ngưỡng điểm để tự động duyệt
BEGIN
    -- Tính điểm validation
    new_score := calculate_company_validation_score(NEW.company_id);
    
    -- Cập nhật điểm validation
    NEW.validation_score := new_score;
    
    -- Kiểm tra điều kiện tự động duyệt
    IF new_score >= approval_threshold AND NEW.status = 'PENDING_APPROVAL' THEN
        NEW.status := 'ACTIVE';
        NEW.is_auto_approved := true;
        NEW.auto_approval_reason := 'Tự động duyệt dựa trên điểm validation: ' || new_score;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger
CREATE TRIGGER trigger_update_company_validation_score
    BEFORE INSERT OR UPDATE ON company
    FOR EACH ROW
    EXECUTE FUNCTION update_company_validation_score();

-- Cập nhật các công ty hiện tại với điểm validation
UPDATE company 
SET validation_score = calculate_company_validation_score(company.company_id),
    is_auto_approved = (validation_score >= 30 AND status = 'ACTIVE'),
    auto_approval_reason = CASE 
        WHEN validation_score >= 30 AND status = 'ACTIVE' THEN 'Tự động duyệt dựa trên điểm validation: ' || validation_score
        ELSE NULL
    END;

