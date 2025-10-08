# 🎉 Create Event with Image Upload API Documentation

## 🎯 Overview

Tài liệu này cung cấp đầy đủ thông tin về API tạo sự kiện với upload ảnh cho Frontend tích hợp.

**Storage:** AWS S3 Cloud Storage (folder: `events/`)  
**Max File Size:** 5MB  
**Supported Formats:** JPEG, JPG, PNG, GIF, WebP  
**Base URL Production:** `https://api.s-pace.com.vn`  
**Base URL Local:** `http://localhost:8080`

---

## 📋 Table of Contents

1. [Create Event with Image](#1-create-event-with-image)
2. [Upload Event Image Only](#2-upload-event-image-only)
3. [Create Event without Image (JSON)](#3-create-event-without-image-json)
4. [Error Handling](#error-handling)
5. [Frontend Integration Examples](#frontend-integration-examples)
6. [Response Examples](#response-examples)

---

## 1. Create Event with Image

Tạo sự kiện mới với upload ảnh cùng lúc.

### 📍 Endpoint

```
POST /api/events/create
```

### 🔐 Authentication

**Required:** Bearer Token (JWT)  
**Roles:** EVENT_MANAGER

### 📥 Request

#### Headers

```http
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data
```

#### Body (Form Data)

| Field           | Type    | Required | Description                                         |
| --------------- | ------- | -------- | --------------------------------------------------- |
| eventName       | String  | Yes      | Tên sự kiện                                         |
| description     | String  | Yes      | Mô tả sự kiện                                       |
| startDate       | String  | Yes      | Ngày bắt đầu (YYYY-MM-DD)                           |
| endDate         | String  | Yes      | Ngày kết thúc (YYYY-MM-DD)                          |
| location        | String  | Yes      | Địa điểm tổ chức                                    |
| maxParticipants | Integer | No       | Số lượng tham gia tối đa                            |
| status          | String  | No       | Trạng thái (DRAFT, PUBLISHED, CANCELLED, COMPLETED) |
| requirements    | String  | No       | Yêu cầu tham gia                                    |
| contactInfo     | String  | No       | Thông tin liên hệ                                   |
| image           | File    | No       | Ảnh sự kiện (JPEG, PNG, GIF, WebP) - Max 5MB        |
| companyId       | UUID    | Yes      | ID công ty tổ chức                                  |

### 📤 Response

#### Success (201 Created)

```json
{
  "success": true,
  "message": "Event created successfully",
  "data": {
    "eventId": "123e4567-e89b-12d3-a456-426614174000",
    "eventName": "Tech Conference 2025",
    "description": "Annual technology conference",
    "startDate": "2025-12-15",
    "endDate": "2025-12-17",
    "location": "Ho Chi Minh City",
    "maxParticipants": 500,
    "status": "DRAFT",
    "requirements": "IT professionals",
    "contactInfo": "contact@company.com",
    "picture": "https://s-pace.s3.amazonaws.com/events/2025/10/08/event_1728388234567.jpg",
    "companyId": "456e7890-e89b-12d3-a456-426614174001",
    "createdAt": "2025-10-08T10:30:00Z",
    "updatedAt": "2025-10-08T10:30:00Z"
  }
}
```

#### Error (400 Bad Request)

```json
{
  "success": false,
  "message": "Event name cannot be null or empty",
  "data": null
}
```

```json
{
  "success": false,
  "message": "File size exceeds maximum limit of 5MB",
  "data": null
}
```

#### Error (401 Unauthorized)

```json
{
  "success": false,
  "message": "Unauthorized - Valid JWT token required",
  "data": null
}
```

#### Error (403 Forbidden)

```json
{
  "success": false,
  "message": "Access denied - Event Manager role required",
  "data": null
}
```

### 🎯 Use Cases

- Tạo sự kiện mới với ảnh bìa
- Event onboarding workflow
- Bulk event creation

### ⚠️ Important Notes

- ✅ Ảnh được lưu vào **AWS S3 folder `events/`**
- ✅ Return S3 URL có thể dùng trực tiếp trong `<img>` tag
- ✅ Chỉ EVENT_MANAGER có thể tạo sự kiện
- ✅ Company ID bắt buộc phải tồn tại

---

## 2. Upload Event Image Only

Chỉ upload ảnh sự kiện (không tạo event).

### 📍 Endpoint

```
POST /api/events/upload-image
```

### 🔐 Authentication

**Required:** Bearer Token (JWT)  
**Roles:** EVENT_MANAGER

### 📥 Request

#### Headers

```http
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data
```

#### Body (Form Data)

| Field | Type | Required | Description                                  |
| ----- | ---- | -------- | -------------------------------------------- |
| image | File | Yes      | Ảnh sự kiện (JPEG, PNG, GIF, WebP) - Max 5MB |

### 📤 Response

#### Success (200 OK)

```json
{
  "success": true,
  "message": "Event image uploaded successfully",
  "data": "https://s-pace.s3.amazonaws.com/events/2025/10/08/event_1728388234567.jpg"
}
```

#### Error (400 Bad Request)

```json
{
  "success": false,
  "message": "File is empty",
  "data": null
}
```

### 🎯 Use Cases

- Upload ảnh trước khi tạo event
- Preview ảnh trước khi submit
- Batch image upload

---

## 3. Create Event without Image (JSON)

Tạo sự kiện không có ảnh (tương thích ngược).

### 📍 Endpoint

```
POST /api/events
```

### 🔐 Authentication

**Required:** Bearer Token (JWT)  
**Roles:** EVENT_MANAGER

### 📥 Request

#### Headers

```http
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

#### Body (JSON)

```json
{
  "eventName": "Tech Conference 2025",
  "description": "Annual technology conference",
  "startDate": "2025-12-15",
  "endDate": "2025-12-17",
  "location": "Ho Chi Minh City",
  "maxParticipants": 500,
  "status": "DRAFT",
  "requirements": "IT professionals",
  "contactInfo": "contact@company.com",
  "picture": null
}
```

#### Query Parameters

| Parameter | Type | Required | Description        |
| --------- | ---- | -------- | ------------------ |
| companyId | UUID | Yes      | ID công ty tổ chức |

### 📤 Response

Tương tự như Create Event with Image, nhưng `picture` sẽ là `null`.

---

## Error Handling

### Common Error Codes

| Status Code | Message                            | Cause                        | Solution                        |
| ----------- | ---------------------------------- | ---------------------------- | ------------------------------- |
| 400         | Event name cannot be null or empty | Thiếu tên sự kiện            | Cung cấp tên sự kiện            |
| 400         | Company ID cannot be null          | Thiếu company ID             | Cung cấp company ID hợp lệ      |
| 400         | File is empty                      | File không có nội dung       | Kiểm tra file trước khi upload  |
| 400         | Invalid file type                  | File không phải image        | Chỉ upload JPEG, PNG, GIF, WebP |
| 400         | File size exceeds maximum limit    | File > 5MB                   | Compress hoặc resize image      |
| 401         | Unauthorized                       | Token không hợp lệ           | Login lại để lấy token mới      |
| 403         | Access denied                      | Không có quyền EVENT_MANAGER | Cần role EVENT_MANAGER          |
| 404         | Company not found                  | Company ID không tồn tại     | Kiểm tra company ID             |
| 500         | Failed to upload event image       | Lỗi server/S3                | Thử lại hoặc báo admin          |

---

## Frontend Integration Examples

### 1. React / Next.js Example

#### Complete Event Creation with Image Upload

```jsx
import React, { useState } from "react";

const CreateEventForm = () => {
  const [formData, setFormData] = useState({
    eventName: "",
    description: "",
    startDate: "",
    endDate: "",
    location: "",
    maxParticipants: "",
    status: "DRAFT",
    requirements: "",
    contactInfo: "",
    companyId: "",
  });
  const [imageFile, setImageFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Validate file size
      if (file.size > 5 * 1024 * 1024) {
        setError("File size must be less than 5MB");
        return;
      }

      // Validate file type
      const allowedTypes = [
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/webp",
      ];
      if (!allowedTypes.includes(file.type)) {
        setError(
          "Invalid file type. Only JPEG, PNG, GIF, and WebP are allowed"
        );
        return;
      }

      setImageFile(file);
      setError("");
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (
      !formData.eventName ||
      !formData.description ||
      !formData.startDate ||
      !formData.endDate ||
      !formData.location ||
      !formData.companyId
    ) {
      setError("Please fill in all required fields");
      return;
    }

    setUploading(true);
    setError("");
    setSuccess("");

    try {
      const token = localStorage.getItem("jwt_token");
      const formDataToSend = new FormData();

      // Add form fields
      Object.keys(formData).forEach((key) => {
        if (formData[key]) {
          formDataToSend.append(key, formData[key]);
        }
      });

      // Add image file if selected
      if (imageFile) {
        formDataToSend.append("image", imageFile);
      }

      const response = await fetch(
        "https://api.s-pace.com.vn/api/events/create",
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formDataToSend,
        }
      );

      const result = await response.json();

      if (result.success) {
        setSuccess("Event created successfully!");
        // Reset form
        setFormData({
          eventName: "",
          description: "",
          startDate: "",
          endDate: "",
          location: "",
          maxParticipants: "",
          status: "DRAFT",
          requirements: "",
          contactInfo: "",
          companyId: "",
        });
        setImageFile(null);
        document.getElementById("imageInput").value = "";
      } else {
        setError(result.message);
      }
    } catch (err) {
      setError("Failed to create event");
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="create-event-form">
      <h2>Create New Event</h2>

      {success && <div className="alert alert-success">{success}</div>}

      {error && <div className="alert alert-error">{error}</div>}

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Event Name *</label>
          <input
            type="text"
            name="eventName"
            value={formData.eventName}
            onChange={handleInputChange}
            required
          />
        </div>

        <div className="form-group">
          <label>Description *</label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            required
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label>Start Date *</label>
            <input
              type="date"
              name="startDate"
              value={formData.startDate}
              onChange={handleInputChange}
              required
            />
          </div>

          <div className="form-group">
            <label>End Date *</label>
            <input
              type="date"
              name="endDate"
              value={formData.endDate}
              onChange={handleInputChange}
              required
            />
          </div>
        </div>

        <div className="form-group">
          <label>Location *</label>
          <input
            type="text"
            name="location"
            value={formData.location}
            onChange={handleInputChange}
            required
          />
        </div>

        <div className="form-group">
          <label>Max Participants</label>
          <input
            type="number"
            name="maxParticipants"
            value={formData.maxParticipants}
            onChange={handleInputChange}
          />
        </div>

        <div className="form-group">
          <label>Status</label>
          <select
            name="status"
            value={formData.status}
            onChange={handleInputChange}
          >
            <option value="DRAFT">Draft</option>
            <option value="PUBLISHED">Published</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </div>

        <div className="form-group">
          <label>Requirements</label>
          <textarea
            name="requirements"
            value={formData.requirements}
            onChange={handleInputChange}
          />
        </div>

        <div className="form-group">
          <label>Contact Info</label>
          <input
            type="text"
            name="contactInfo"
            value={formData.contactInfo}
            onChange={handleInputChange}
          />
        </div>

        <div className="form-group">
          <label>Company ID *</label>
          <input
            type="text"
            name="companyId"
            value={formData.companyId}
            onChange={handleInputChange}
            required
          />
        </div>

        <div className="form-group">
          <label>Event Image</label>
          <input
            id="imageInput"
            type="file"
            accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
            onChange={handleFileChange}
          />
          <small>Max 5MB, JPEG/PNG/GIF/WebP only</small>
        </div>

        {imageFile && (
          <div className="image-preview">
            <img
              src={URL.createObjectURL(imageFile)}
              alt="Preview"
              style={{ width: 200, height: 150, objectFit: "cover" }}
            />
          </div>
        )}

        <button type="submit" disabled={uploading} className="btn btn-primary">
          {uploading ? "Creating Event..." : "Create Event"}
        </button>
      </form>
    </div>
  );
};

export default CreateEventForm;
```

#### Using Axios

```javascript
import axios from "axios";

const createEventWithImage = async (eventData, imageFile) => {
  const token = localStorage.getItem("jwt_token");

  const formData = new FormData();

  // Add event data
  Object.keys(eventData).forEach((key) => {
    if (eventData[key]) {
      formData.append(key, eventData[key]);
    }
  });

  // Add image file if provided
  if (imageFile) {
    formData.append("image", imageFile);
  }

  try {
    const response = await axios.post(
      "https://api.s-pace.com.vn/api/events/create",
      formData,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "multipart/form-data",
        },
      }
    );

    if (response.data.success) {
      return response.data.data; // Event object
    } else {
      throw new Error(response.data.message);
    }
  } catch (error) {
    if (error.response) {
      throw new Error(error.response.data.message);
    }
    throw error;
  }
};

// Usage
const handleCreateEvent = async () => {
  try {
    const eventData = {
      eventName: "Tech Conference 2025",
      description: "Annual technology conference",
      startDate: "2025-12-15",
      endDate: "2025-12-17",
      location: "Ho Chi Minh City",
      maxParticipants: 500,
      status: "DRAFT",
      companyId: "456e7890-e89b-12d3-a456-426614174001",
    };

    const imageFile = document.getElementById("imageInput").files[0];
    const createdEvent = await createEventWithImage(eventData, imageFile);

    console.log("Event created:", createdEvent);
    alert("Event created successfully!");
  } catch (error) {
    console.error("Error creating event:", error.message);
    alert("Failed to create event: " + error.message);
  }
};
```

### 2. Vue.js Example

```vue
<template>
  <div class="create-event-form">
    <h2>Create New Event</h2>

    <div v-if="success" class="alert alert-success">
      {{ success }}
    </div>

    <div v-if="error" class="alert alert-error">
      {{ error }}
    </div>

    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>Event Name *</label>
        <input type="text" v-model="formData.eventName" required />
      </div>

      <div class="form-group">
        <label>Description *</label>
        <textarea v-model="formData.description" required />
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>Start Date *</label>
          <input type="date" v-model="formData.startDate" required />
        </div>

        <div class="form-group">
          <label>End Date *</label>
          <input type="date" v-model="formData.endDate" required />
        </div>
      </div>

      <div class="form-group">
        <label>Location *</label>
        <input type="text" v-model="formData.location" required />
      </div>

      <div class="form-group">
        <label>Max Participants</label>
        <input type="number" v-model="formData.maxParticipants" />
      </div>

      <div class="form-group">
        <label>Status</label>
        <select v-model="formData.status">
          <option value="DRAFT">Draft</option>
          <option value="PUBLISHED">Published</option>
          <option value="CANCELLED">Cancelled</option>
          <option value="COMPLETED">Completed</option>
        </select>
      </div>

      <div class="form-group">
        <label>Requirements</label>
        <textarea v-model="formData.requirements" />
      </div>

      <div class="form-group">
        <label>Contact Info</label>
        <input type="text" v-model="formData.contactInfo" />
      </div>

      <div class="form-group">
        <label>Company ID *</label>
        <input type="text" v-model="formData.companyId" required />
      </div>

      <div class="form-group">
        <label>Event Image</label>
        <input
          type="file"
          accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
          @change="handleFileChange"
        />
        <small>Max 5MB, JPEG/PNG/GIF/WebP only</small>
      </div>

      <div v-if="imagePreview" class="image-preview">
        <img
          :src="imagePreview"
          alt="Preview"
          style="width: 200px; height: 150px; object-fit: cover;"
        />
      </div>

      <button type="submit" :disabled="uploading" class="btn btn-primary">
        {{ uploading ? "Creating Event..." : "Create Event" }}
      </button>
    </form>
  </div>
</template>

<script>
export default {
  data() {
    return {
      formData: {
        eventName: "",
        description: "",
        startDate: "",
        endDate: "",
        location: "",
        maxParticipants: "",
        status: "DRAFT",
        requirements: "",
        contactInfo: "",
        companyId: "",
      },
      imageFile: null,
      imagePreview: null,
      uploading: false,
      error: "",
      success: "",
    };
  },
  methods: {
    handleFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        // Validate file size
        if (file.size > 5 * 1024 * 1024) {
          this.error = "File size must be less than 5MB";
          return;
        }

        // Validate file type
        const allowedTypes = [
          "image/jpeg",
          "image/jpg",
          "image/png",
          "image/gif",
          "image/webp",
        ];
        if (!allowedTypes.includes(file.type)) {
          this.error =
            "Invalid file type. Only JPEG, PNG, GIF, and WebP are allowed";
          return;
        }

        this.imageFile = file;
        this.imagePreview = URL.createObjectURL(file);
        this.error = "";
      }
    },

    async handleSubmit() {
      if (
        !this.formData.eventName ||
        !this.formData.description ||
        !this.formData.startDate ||
        !this.formData.endDate ||
        !this.formData.location ||
        !this.formData.companyId
      ) {
        this.error = "Please fill in all required fields";
        return;
      }

      this.uploading = true;
      this.error = "";
      this.success = "";

      try {
        const token = localStorage.getItem("jwt_token");
        const formData = new FormData();

        // Add form fields
        Object.keys(this.formData).forEach((key) => {
          if (this.formData[key]) {
            formData.append(key, this.formData[key]);
          }
        });

        // Add image file if selected
        if (this.imageFile) {
          formData.append("image", this.imageFile);
        }

        const response = await fetch(
          "https://api.s-pace.com.vn/api/events/create",
          {
            method: "POST",
            headers: {
              Authorization: `Bearer ${token}`,
            },
            body: formData,
          }
        );

        const result = await response.json();

        if (result.success) {
          this.success = "Event created successfully!";
          // Reset form
          this.formData = {
            eventName: "",
            description: "",
            startDate: "",
            endDate: "",
            location: "",
            maxParticipants: "",
            status: "DRAFT",
            requirements: "",
            contactInfo: "",
            companyId: "",
          };
          this.imageFile = null;
          this.imagePreview = null;
        } else {
          this.error = result.message;
        }
      } catch (err) {
        this.error = "Failed to create event";
      } finally {
        this.uploading = false;
      }
    },
  },
};
</script>
```

### 3. Two-Step Upload (Upload Image First, Then Create Event)

```javascript
// Step 1: Upload image first
const uploadEventImage = async (file) => {
  const token = localStorage.getItem("jwt_token");
  const formData = new FormData();
  formData.append("image", file);

  const response = await fetch(
    "https://api.s-pace.com.vn/api/events/upload-image",
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
      },
      body: formData,
    }
  );

  const result = await response.json();

  if (result.success) {
    return result.data; // S3 URL
  } else {
    throw new Error(result.message);
  }
};

// Step 2: Create event with image URL
const createEvent = async (eventData, imageUrl) => {
  const token = localStorage.getItem("jwt_token");

  const eventPayload = {
    ...eventData,
    picture: imageUrl,
  };

  const response = await fetch(
    `https://api.s-pace.com.vn/api/events?companyId=${eventData.companyId}`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(eventPayload),
    }
  );

  const result = await response.json();

  if (result.success) {
    return result.data;
  } else {
    throw new Error(result.message);
  }
};

// Usage
const handleCreateEventWithPreview = async (eventData, imageFile) => {
  try {
    // Step 1: Upload image
    const imageUrl = await uploadEventImage(imageFile);
    console.log("Image uploaded:", imageUrl);

    // Step 2: Create event with image URL
    const createdEvent = await createEvent(eventData, imageUrl);
    console.log("Event created:", createdEvent);

    alert("Event created successfully!");
  } catch (error) {
    console.error("Error:", error.message);
    alert("Failed to create event: " + error.message);
  }
};
```

---

## Response Examples

### Success Response Structure

```json
{
  "success": true,
  "message": "Event created successfully",
  "data": {
    "eventId": "123e4567-e89b-12d3-a456-426614174000",
    "eventName": "Tech Conference 2025",
    "description": "Annual technology conference",
    "startDate": "2025-12-15",
    "endDate": "2025-12-17",
    "location": "Ho Chi Minh City",
    "maxParticipants": 500,
    "status": "DRAFT",
    "requirements": "IT professionals",
    "contactInfo": "contact@company.com",
    "picture": "https://s-pace.s3.amazonaws.com/events/2025/10/08/event_1728388234567.jpg",
    "companyId": "456e7890-e89b-12d3-a456-426614174001",
    "createdAt": "2025-10-08T10:30:00Z",
    "updatedAt": "2025-10-08T10:30:00Z"
  }
}
```

### Error Response Structure

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

### All Possible Error Messages

```json
// Validation errors
{ "success": false, "message": "Event name cannot be null or empty", "data": null }
{ "success": false, "message": "Company ID cannot be null", "data": null }
{ "success": false, "message": "File is empty", "data": null }
{ "success": false, "message": "Invalid file type. Only JPEG, PNG, GIF, and WebP are allowed", "data": null }
{ "success": false, "message": "File size exceeds maximum limit of 5MB", "data": null }

// Authentication errors
{ "success": false, "message": "Unauthorized - Valid JWT token required", "data": null }
{ "success": false, "message": "Invalid or missing token", "data": null }

// Authorization errors
{ "success": false, "message": "Access denied - Event Manager role required", "data": null }

// Not found errors
{ "success": false, "message": "Company not found", "data": null }

// Server errors
{ "success": false, "message": "Failed to upload event image: S3 connection error", "data": null }
{ "success": false, "message": "Failed to create event", "data": null }
```

---

## 🧪 Testing with cURL

### Test Create Event with Image

```bash
curl -X POST "https://api.s-pace.com.vn/api/events/create" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "eventName=Tech Conference 2025" \
  -F "description=Annual technology conference" \
  -F "startDate=2025-12-15" \
  -F "endDate=2025-12-17" \
  -F "location=Ho Chi Minh City" \
  -F "maxParticipants=500" \
  -F "status=DRAFT" \
  -F "requirements=IT professionals" \
  -F "contactInfo=contact@company.com" \
  -F "companyId=456e7890-e89b-12d3-a456-426614174001" \
  -F "image=@/path/to/event-image.jpg"
```

### Test Upload Image Only

```bash
curl -X POST "https://api.s-pace.com.vn/api/events/upload-image" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "image=@/path/to/event-image.jpg"
```

### Test Create Event without Image (JSON)

```bash
curl -X POST "https://api.s-pace.com.vn/api/events?companyId=456e7890-e89b-12d3-a456-426614174001" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "eventName": "Tech Conference 2025",
    "description": "Annual technology conference",
    "startDate": "2025-12-15",
    "endDate": "2025-12-17",
    "location": "Ho Chi Minh City",
    "maxParticipants": 500,
    "status": "DRAFT",
    "requirements": "IT professionals",
    "contactInfo": "contact@company.com",
    "picture": null
  }'
```

---

## 🔧 Troubleshooting

### Issue: CORS Error

**Solution:** Backend đã cấu hình CORS. Đảm bảo request có header `Authorization`.

### Issue: 401 Unauthorized

**Solution:**

- Kiểm tra JWT token có hợp lệ không
- Token có expired không
- Login lại để lấy token mới

### Issue: 403 Forbidden

**Solution:**

- Cần role EVENT_MANAGER
- Kiểm tra user có quyền tạo event không

### Issue: 413 Payload Too Large

**Solution:**

- File > 5MB
- Compress hoặc resize image trước khi upload

### Issue: 500 Internal Server Error

**Solution:**

- Kiểm tra AWS S3 credentials
- Kiểm tra network connection
- Xem server logs để biết chi tiết

---

## 📞 Support

- **API Documentation:** `https://api.s-pace.com.vn/swagger-ui/index.html`
- **Backend Repository:** Contact your backend team
- **Issues:** Report to your project manager

---

## 📝 Changelog

### Version 1.0 (2025-10-08)

- ✅ Initial release
- ✅ Create event with image upload API
- ✅ Upload event image only API
- ✅ AWS S3 integration (events folder)
- ✅ File validation (type, size)
- ✅ Swagger UI file upload support
- ✅ Server selection (localhost/production)

---

**Last Updated:** 2025-10-08  
**API Version:** 1.0  
**Document Version:** 1.0
