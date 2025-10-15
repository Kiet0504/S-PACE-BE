# API Tạo Sự Kiện - Event Creation API Documentation

## Tổng Quan (Overview)

API này cho phép tạo mới sự kiện trong hệ thống S-PACE. Có 2 cách để tạo sự kiện:

1. **Tạo sự kiện với hình ảnh** (Multipart Form Data)
2. **Tạo sự kiện không có hình ảnh** (JSON)

## Xác Thực (Authentication)

- **Yêu cầu**: JWT Token hợp lệ trong header `Authorization: Bearer <token>`
- **Quyền hạn**: Chỉ người dùng có vai trò `EVENT_MANAGER` mới có thể tạo sự kiện
- **Tự động set `created_by`**: Hệ thống tự động lấy user ID từ JWT token và set làm người tạo sự kiện

---

## 1. Tạo Sự Kiện Với Hình Ảnh

### Endpoint

```
POST /api/events/create
```

### Content-Type

```
multipart/form-data
```

### Tham Số (Parameters)

| Tham số           | Loại    | Bắt buộc | Mô tả                                               |
| ----------------- | ------- | -------- | --------------------------------------------------- |
| `eventName`       | String  | ✅       | Tên sự kiện                                         |
| `description`     | String  | ✅       | Mô tả chi tiết sự kiện                              |
| `startDate`       | String  | ✅       | Ngày bắt đầu (format: YYYY-MM-DD)                   |
| `endDate`         | String  | ✅       | Ngày kết thúc (format: YYYY-MM-DD)                  |
| `location`        | String  | ✅       | Địa điểm tổ chức                                    |
| `companyId`       | UUID    | ✅       | ID của công ty                                      |
| `maxParticipants` | Integer | ❌       | Số lượng người tham gia tối đa                      |
| `status`          | String  | ❌       | Trạng thái sự kiện (mặc định: DRAFT)                |
| `requirements`    | String  | ❌       | Yêu cầu tham gia                                    |
| `contactInfo`     | String  | ❌       | Thông tin liên hệ                                   |
| `image`           | File    | ❌       | Hình ảnh sự kiện (JPEG, PNG, GIF, WebP, tối đa 5MB) |

### Các Trạng Thái Sự Kiện (Event Status)

- `ACTIVE` - Đang hoạt động
- `DRAFT` - Bản nháp (mặc định)
- `PUBLISHED` - Đã xuất bản
- `REGISTRATION_OPEN` - Mở đăng ký
- `REGISTRATION_CLOSED` - Đóng đăng ký
- `ONGOING` - Đang diễn ra
- `COMPLETED` - Đã hoàn thành
- `CANCELLED` - Đã hủy
- `SUSPENDED` - Tạm dừng

### Ví Dụ Request (cURL)

```bash
curl -X POST "http://localhost:8080/api/events/create" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "eventName=Tech Conference 2024" \
  -F "description=Hội nghị công nghệ thông tin năm 2024" \
  -F "startDate=2024-06-15" \
  -F "endDate=2024-06-17" \
  -F "location=Trung tâm Hội nghị Quốc gia" \
  -F "companyId=123e4567-e89b-12d3-a456-426614174000" \
  -F "maxParticipants=500" \
  -F "status=DRAFT" \
  -F "requirements=Yêu cầu có kinh nghiệm IT" \
  -F "contactInfo=contact@techconf.com" \
  -F "image=@/path/to/event-image.jpg"
```

### Ví Dụ Response (Thành Công)

```json
{
  "success": true,
  "message": "Event created successfully",
  "data": {
    "eventId": "456e7890-e89b-12d3-a456-426614174001",
    "eventName": "Tech Conference 2024",
    "description": "Hội nghị công nghệ thông tin năm 2024",
    "startDate": "2024-06-15",
    "endDate": "2024-06-17",
    "location": "Trung tâm Hội nghị Quốc gia",
    "maxParticipants": 500,
    "status": "DRAFT",
    "requirements": "Yêu cầu có kinh nghiệm IT",
    "contactInfo": "contact@techconf.com",
    "picture": "https://s3.amazonaws.com/bucket/events/event-image.jpg",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

---

## 2. Tạo Sự Kiện Không Có Hình Ảnh

### Endpoint

```
POST /api/events?companyId={companyId}
```

### Content-Type

```
application/json
```

### Request Body

```json
{
  "eventName": "Tech Conference 2024",
  "description": "Hội nghị công nghệ thông tin năm 2024",
  "startDate": "2024-06-15",
  "endDate": "2024-06-17",
  "location": "Trung tâm Hội nghị Quốc gia",
  "maxParticipants": 500,
  "status": "DRAFT",
  "requirements": "Yêu cầu có kinh nghiệm IT",
  "contactInfo": "contact@techconf.com",
  "picture": null
}
```

### Ví Dụ Request (cURL)

```bash
curl -X POST "http://localhost:8080/api/events?companyId=123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "eventName": "Tech Conference 2024",
    "description": "Hội nghị công nghệ thông tin năm 2024",
    "startDate": "2024-06-15",
    "endDate": "2024-06-17",
    "location": "Trung tâm Hội nghị Quốc gia",
    "maxParticipants": 500,
    "status": "DRAFT",
    "requirements": "Yêu cầu có kinh nghiệm IT",
    "contactInfo": "contact@techconf.com"
  }'
```

---

## 3. Upload Hình Ảnh Riêng Biệt

### Endpoint

```
POST /api/events/upload-image
```

### Content-Type

```
multipart/form-data
```

### Tham Số

| Tham số | Loại | Bắt buộc | Mô tả                                               |
| ------- | ---- | -------- | --------------------------------------------------- |
| `image` | File | ✅       | Hình ảnh sự kiện (JPEG, PNG, GIF, WebP, tối đa 5MB) |

### Ví Dụ Request

```bash
curl -X POST "http://localhost:8080/api/events/upload-image" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "image=@/path/to/event-image.jpg"
```

### Ví Dụ Response

```json
{
  "success": true,
  "message": "Event image uploaded successfully",
  "data": "https://s3.amazonaws.com/bucket/events/uploaded-image.jpg"
}
```

---

## Mã Lỗi (Error Codes)

### 400 Bad Request

```json
{
  "success": false,
  "message": "Event name cannot be null or empty",
  "data": null
}
```

### 403 Forbidden

```json
{
  "success": false,
  "message": "Access denied - Event Manager role required",
  "data": null
}
```

### 500 Internal Server Error

```json
{
  "success": false,
  "message": "Failed to create event",
  "data": null
}
```

---

## Validation Rules

### Bắt Buộc (Required)

- `eventName`: Không được null hoặc rỗng
- `description`: Không được null hoặc rỗng
- `startDate`: Phải là ngày hợp lệ (YYYY-MM-DD)
- `endDate`: Phải là ngày hợp lệ và sau startDate
- `location`: Không được null hoặc rỗng
- `companyId`: Phải là UUID hợp lệ
- **JWT Token**: Phải có token hợp lệ để xác định `created_by`

### Tùy Chọn (Optional)

- `maxParticipants`: Số nguyên dương
- `status`: Một trong các giá trị enum EventStatus
- `requirements`: Chuỗi văn bản
- `contactInfo`: Chuỗi văn bản
- `image`: File hình ảnh (JPEG, PNG, GIF, WebP, tối đa 5MB)

---

## Xử Lý Tự Động (Automatic Processing)

### 1. Tự Động Set `created_by`

- Hệ thống tự động lấy `userId` từ JWT token
- Set làm `created_by` cho sự kiện mới tạo
- Không cần truyền thêm tham số `created_by` trong request

### 2. Tự Động Set Timestamps

- `createdAt`: Tự động set khi tạo sự kiện
- `updatedAt`: Tự động set khi tạo và cập nhật sự kiện

### 3. Tự Động Set Status Mặc Định

- Nếu không chỉ định `status`, sự kiện sẽ có trạng thái `DRAFT`

---

## Lưu Ý Quan Trọng

1. **Quyền Truy Cập**: Chỉ người dùng có vai trò `EVENT_MANAGER` mới có thể tạo sự kiện
2. **Tự Động Set Người Tạo**: Hệ thống tự động lấy user ID từ JWT token làm `created_by`
3. **Hình Ảnh**: Được lưu trữ trên AWS S3 và trả về URL công khai
4. **Ngày Tháng**: Sử dụng format ISO 8601 (YYYY-MM-DD)
5. **Trạng Thái Mặc Định**: Nếu không chỉ định, sự kiện sẽ có trạng thái `DRAFT`
6. **Kích Thước File**: Hình ảnh tối đa 5MB
7. **Định Dạng Hình Ảnh**: Chỉ chấp nhận JPEG, PNG, GIF, WebP

---

## Ví Dụ Sử Dụng JavaScript (Frontend)

```javascript
// Tạo sự kiện với hình ảnh
async function createEventWithImage(eventData, imageFile) {
  const formData = new FormData();
  formData.append("eventName", eventData.eventName);
  formData.append("description", eventData.description);
  formData.append("startDate", eventData.startDate);
  formData.append("endDate", eventData.endDate);
  formData.append("location", eventData.location);
  formData.append("companyId", eventData.companyId);

  if (imageFile) {
    formData.append("image", imageFile);
  }

  try {
    const response = await fetch("/api/events/create", {
      method: "POST",
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
      body: formData,
    });

    const result = await response.json();

    if (result.success) {
      console.log("Event created:", result.data);
      return result.data;
    } else {
      console.error("Error:", result.message);
      throw new Error(result.message);
    }
  } catch (error) {
    console.error("Failed to create event:", error);
    throw error;
  }
}

// Tạo sự kiện không có hình ảnh
async function createEvent(eventData) {
  try {
    const response = await fetch(
      `/api/events?companyId=${eventData.companyId}`,
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(eventData),
      }
    );

    const result = await response.json();

    if (result.success) {
      console.log("Event created:", result.data);
      return result.data;
    } else {
      console.error("Error:", result.message);
      throw new Error(result.message);
    }
  } catch (error) {
    console.error("Failed to create event:", error);
    throw error;
  }
}
```

---

## Thay Đổi Gần Đây

### ✅ Đã Sửa Lỗi `created_by`

- **Vấn đề**: Lỗi `null value in column "created_by" of relation "event" violates not-null constraint`
- **Giải pháp**: Tự động lấy `userId` từ JWT token và set làm `created_by`
- **Cách thức**: Không cần thay đổi API request, hệ thống tự động xử lý

### 🔧 Cập Nhật Code

1. **EventController**: Thêm logic lấy `userId` từ JWT token
2. **EventService**: Thêm parameter `createdBy` vào method `createEvent`
3. **EventServiceImpl**: Set `createdBy` khi tạo event entity

---

_Tài liệu này được tạo dựa trên code hiện tại của hệ thống S-PACE. Đã cập nhật để xử lý lỗi `created_by` constraint._
