# Event Status API Documentation - Backend Requirements

## 📋 Tổng quan

Tài liệu này mô tả các API cần thiết để Backend hỗ trợ việc cập nhật trạng thái sự kiện (Event Status) trong hệ thống S-Pace.

---

## 🎯 Các trạng thái sự kiện hiện tại

Hệ thống hiện tại hỗ trợ 7 trạng thái sự kiện:

| Trạng thái       | Mã                    | Mô tả                                                   | Màu sắc    |
| ---------------- | --------------------- | ------------------------------------------------------- | ---------- |
| **Nháp**         | `DRAFT`               | Sự kiện đang được soạn thảo, chưa công khai             | Vàng       |
| **Công khai**    | `PUBLISHED`           | Sự kiện đã được công khai, mọi người có thể xem         | Xanh lá    |
| **Mở đăng ký**   | `REGISTRATION_OPEN`   | Sự kiện đang mở đăng ký cho người tham gia              | Xanh dương |
| **Đóng đăng ký** | `REGISTRATION_CLOSED` | Sự kiện đã đóng đăng ký, không nhận thêm người tham gia | Cam        |
| **Đang diễn ra** | `ONGOING`             | Sự kiện đang được tổ chức                               | Tím        |
| **Hoàn thành**   | `COMPLETED`           | Sự kiện đã kết thúc thành công                          | Xanh ngọc  |
| **Đã hủy**       | `CANCELLED`           | Sự kiện đã bị hủy bỏ                                    | Đỏ         |

---

## 🔧 API Endpoints cần thiết

### 1. **Cập nhật trạng thái sự kiện (Chính)**

#### **PUT** `/api/events/{eventId}/status`

**Mô tả:** Cập nhật trạng thái của một sự kiện cụ thể.

**Headers:**

```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**

```json
{
  "status": "REGISTRATION_OPEN"
}
```

**Response Success (200):**

```json
{
  "success": true,
  "message": "Event status updated successfully",
  "data": {
    "eventId": "bac97124-4ce8-49fd-a4dd-ac9c132c1bef",
    "eventName": "Tech Conference 2024",
    "status": "REGISTRATION_OPEN",
    "updatedAt": "2024-01-15T10:30:00Z"
  }
}
```

**Response Error (400):**

```json
{
  "success": false,
  "message": "Invalid status transition",
  "errors": {
    "status": "Cannot change from COMPLETED to DRAFT"
  }
}
```

**Response Error (404):**

```json
{
  "success": false,
  "message": "Event not found",
  "errors": {
    "eventId": "Event with ID bac97124-4ce8-49fd-a4dd-ac9c132c1bef not found"
  }
}
```

---

### 2. **Lấy thông tin sự kiện với trạng thái**

#### **GET** `/api/events/{eventId}`

**Mô tả:** Lấy thông tin chi tiết của sự kiện bao gồm trạng thái hiện tại.

**Headers:**

```
Authorization: Bearer {token}
```

**Response Success (200):**

```json
{
  "success": true,
  "message": "Event retrieved successfully",
  "data": {
    "eventId": "bac97124-4ce8-49fd-a4dd-ac9c132c1bef",
    "eventName": "Tech Conference 2024",
    "description": "Annual technology conference",
    "startDate": "2024-12-15",
    "endDate": "2024-12-17",
    "location": "Ho Chi Minh City Convention Center",
    "maxParticipants": 500,
    "currentParticipants": 150,
    "status": "REGISTRATION_OPEN",
    "createdBy": "user-uuid-here",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-15T10:30:00Z",
    "requirements": "Basic programming knowledge",
    "contactInfo": "contact@techconf.com",
    "picture": "https://s3.amazonaws.com/event-images/tech-conf-2024.jpg"
  }
}
```

---

### 3. **Lấy danh sách sự kiện theo trạng thái**

#### **GET** `/api/events/status/{status}`

**Mô tả:** Lấy danh sách tất cả sự kiện có trạng thái cụ thể.

**Headers:**

```
Authorization: Bearer {token}
```

**Query Parameters:**

- `page` (optional): Số trang (default: 1)
- `limit` (optional): Số lượng sự kiện mỗi trang (default: 10)
- `sortBy` (optional): Sắp xếp theo trường nào (default: "createdAt")
- `order` (optional): Thứ tự sắp xếp "asc" hoặc "desc" (default: "desc")

**Example Request:**

```
GET /api/events/status/REGISTRATION_OPEN?page=1&limit=20&sortBy=startDate&order=asc
```

**Response Success (200):**

```json
{
  "success": true,
  "message": "Events retrieved successfully",
  "data": [
    {
      "eventId": "bac97124-4ce8-49fd-a4dd-ac9c132c1bef",
      "eventName": "Tech Conference 2024",
      "status": "REGISTRATION_OPEN",
      "startDate": "2024-12-15",
      "endDate": "2024-12-17",
      "location": "Ho Chi Minh City",
      "maxParticipants": 500,
      "currentParticipants": 150
    }
  ],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 1,
    "totalPages": 1
  }
}
```

---

## 🔄 Quy tắc chuyển đổi trạng thái

Backend cần implement logic validation để đảm bảo các chuyển đổi trạng thái hợp lệ:

### **Sơ đồ chuyển đổi trạng thái:**

```
DRAFT → PUBLISHED → REGISTRATION_OPEN → REGISTRATION_CLOSED → ONGOING → COMPLETED
  ↓         ↓              ↓                    ↓                ↓
CANCELLED ← CANCELLED ← CANCELLED ← CANCELLED ← CANCELLED
```

### **Quy tắc validation:**

1. **DRAFT** → Có thể chuyển thành: `PUBLISHED`, `CANCELLED`
2. **PUBLISHED** → Có thể chuyển thành: `REGISTRATION_OPEN`, `CANCELLED`
3. **REGISTRATION_OPEN** → Có thể chuyển thành: `REGISTRATION_CLOSED`, `CANCELLED`
4. **REGISTRATION_CLOSED** → Có thể chuyển thành: `ONGOING`, `CANCELLED`
5. **ONGOING** → Có thể chuyển thành: `COMPLETED`, `CANCELLED`
6. **COMPLETED** → Không thể chuyển sang trạng thái khác
7. **CANCELLED** → Không thể chuyển sang trạng thái khác

---

## 🛡️ Bảo mật và Phân quyền

### **Phân quyền cập nhật trạng thái:**

- **EVENT_MANAGER**: Có thể cập nhật trạng thái sự kiện của công ty mình
- **COMPANY_ADMIN**: Có thể cập nhật trạng thái sự kiện của công ty mình
- **ADMIN**: Có thể cập nhật trạng thái của tất cả sự kiện

### **Validation cần thiết:**

1. **Authentication**: Kiểm tra token hợp lệ
2. **Authorization**: Kiểm tra quyền truy cập sự kiện
3. **Event Ownership**: Chỉ cho phép cập nhật sự kiện của công ty mình
4. **Status Transition**: Kiểm tra chuyển đổi trạng thái hợp lệ

---

## 📊 Database Schema

### **Event Table cần có các trường:**

```sql
CREATE TABLE events (
    event_id VARCHAR(36) PRIMARY KEY,
    event_name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    location VARCHAR(255) NOT NULL,
    max_participants INT,
    current_participants INT DEFAULT 0,
    status ENUM(
        'DRAFT',
        'PUBLISHED',
        'REGISTRATION_OPEN',
        'REGISTRATION_CLOSED',
        'ONGOING',
        'COMPLETED',
        'CANCELLED'
    ) DEFAULT 'DRAFT',
    created_by VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    requirements TEXT,
    contact_info VARCHAR(255),
    picture VARCHAR(500),

    FOREIGN KEY (created_by) REFERENCES users(user_id),
    FOREIGN KEY (company_id) REFERENCES companies(company_id),
    INDEX idx_status (status),
    INDEX idx_company_id (company_id),
    INDEX idx_created_by (created_by)
);
```

---

## 🔧 Implementation Notes

### **1. Event Status Service (Backend)**

```java
@Service
public class EventStatusService {

    public Event updateEventStatus(String eventId, String newStatus, String userId) {
        // 1. Validate user permissions
        // 2. Get current event
        // 3. Validate status transition
        // 4. Update status
        // 5. Log status change
        // 6. Return updated event
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        // Implement transition rules
    }

    private void logStatusChange(String eventId, String oldStatus, String newStatus, String userId) {
        // Log status change for audit trail
    }
}
```

### **2. Event Status Controller (Backend)**

```java
@RestController
@RequestMapping("/api/events")
public class EventStatusController {

    @PutMapping("/{eventId}/status")
    public ResponseEntity<ApiResponse<Event>> updateEventStatus(
            @PathVariable String eventId,
            @RequestBody UpdateStatusRequest request,
            Authentication authentication) {

        // Implementation
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Event>>> getEventsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        // Implementation
    }
}
```

---

## 🧪 Testing Requirements

### **Unit Tests cần thiết:**

1. **Status Transition Tests:**

   - Test valid transitions
   - Test invalid transitions
   - Test edge cases

2. **Permission Tests:**

   - Test EVENT_MANAGER permissions
   - Test COMPANY_ADMIN permissions
   - Test unauthorized access

3. **Integration Tests:**
   - Test complete status update flow
   - Test error handling
   - Test concurrent updates

---

## 📝 Error Handling

### **Các lỗi cần xử lý:**

1. **400 Bad Request:**

   - Invalid status value
   - Invalid status transition
   - Missing required fields

2. **401 Unauthorized:**

   - Missing or invalid token
   - Expired token

3. **403 Forbidden:**

   - Insufficient permissions
   - Not event owner

4. **404 Not Found:**

   - Event not found
   - Invalid event ID

5. **500 Internal Server Error:**
   - Database errors
   - System errors

---

## 🚀 Deployment Checklist

- [ ] Database migration for status enum
- [ ] API endpoints implementation
- [ ] Permission validation
- [ ] Status transition logic
- [ ] Error handling
- [ ] Unit tests
- [ ] Integration tests
- [ ] API documentation
- [ ] Frontend integration testing

---

## 📞 Liên hệ

Nếu có thắc mắc về implementation, vui lòng liên hệ team Frontend để được hỗ trợ.

**Frontend Team Contact:**

- Email: frontend@s-pace.com
- Slack: #frontend-team

---

_Tài liệu này được tạo bởi Frontend Team - S-Pace Platform_
_Cập nhật lần cuối: 15/01/2024_
