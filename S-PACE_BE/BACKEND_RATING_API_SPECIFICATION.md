# Backend Rating API Specification

## Yêu cầu cho Backend Team

### 📋 Tổng Quan

Frontend đã implement đầy đủ rating system cho CTV (Cộng Tác Viên). Backend cần implement các API endpoints tương ứng để hỗ trợ tính năng này.

---

## 🗄️ Database Schema

### 1. Bảng `rating`

```sql
CREATE TABLE rating (
    rating_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    collaborator_id UUID NOT NULL,
    rated_by UUID NOT NULL,
    rating_score DECIMAL(3,2) NOT NULL CHECK (rating_score >= 1.0 AND rating_score <= 5.0),
    rating_comment TEXT,
    rating_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    punctuality_score DECIMAL(3,2) DEFAULT 0.00 CHECK (punctuality_score >= 0.0 AND punctuality_score <= 5.0),
    quality_score DECIMAL(3,2) DEFAULT 0.00 CHECK (quality_score >= 0.0 AND quality_score <= 5.0),
    attitude_score DECIMAL(3,2) DEFAULT 0.00 CHECK (attitude_score >= 0.0 AND attitude_score <= 5.0),
    teamwork_score DECIMAL(3,2) DEFAULT 0.00 CHECK (teamwork_score >= 0.0 AND teamwork_score <= 5.0),

    -- Constraints
    CONSTRAINT fk_rating_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_collaborator FOREIGN KEY (collaborator_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_rated_by FOREIGN KEY (rated_by) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT unique_rating_per_event_collaborator UNIQUE (event_id, collaborator_id, rated_by)
);

-- Indexes for performance
CREATE INDEX idx_rating_event_id ON rating(event_id);
CREATE INDEX idx_rating_collaborator_id ON rating(collaborator_id);
CREATE INDEX idx_rating_rated_by ON rating(rated_by);
CREATE INDEX idx_rating_date ON rating(rating_date);
```

### 2. Cập nhật bảng `users`

```sql
ALTER TABLE users
ADD COLUMN average_rating DECIMAL(3,2) DEFAULT 0.00,
ADD COLUMN total_ratings INTEGER DEFAULT 0;

-- Index for performance
CREATE INDEX idx_users_average_rating ON users(average_rating);
```

### 3. Trigger tự động cập nhật rating stats

```sql
-- Function to update user rating statistics
CREATE OR REPLACE FUNCTION update_user_rating_stats(user_uuid UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE users
    SET
        average_rating = (
            SELECT COALESCE(AVG(rating_score), 0.00)
            FROM rating
            WHERE collaborator_id = user_uuid
        ),
        total_ratings = (
            SELECT COUNT(*)
            FROM rating
            WHERE collaborator_id = user_uuid
        )
    WHERE user_id = user_uuid;
END;
$$ LANGUAGE plpgsql;

-- Trigger after insert
CREATE OR REPLACE FUNCTION trigger_update_rating_stats_insert()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM update_user_rating_stats(NEW.collaborator_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_rating_insert_stats
    AFTER INSERT ON rating
    FOR EACH ROW
    EXECUTE FUNCTION trigger_update_rating_stats_insert();

-- Trigger after update
CREATE OR REPLACE FUNCTION trigger_update_rating_stats_update()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM update_user_rating_stats(NEW.collaborator_id);
    IF OLD.collaborator_id != NEW.collaborator_id THEN
        PERFORM update_user_rating_stats(OLD.collaborator_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_rating_update_stats
    AFTER UPDATE ON rating
    FOR EACH ROW
    EXECUTE FUNCTION trigger_update_rating_stats_update();

-- Trigger after delete
CREATE OR REPLACE FUNCTION trigger_update_rating_stats_delete()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM update_user_rating_stats(OLD.collaborator_id);
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_rating_delete_stats
    AFTER DELETE ON rating
    FOR EACH ROW
    EXECUTE FUNCTION trigger_update_rating_stats_delete();
```

---

## 🔌 API Endpoints

### Base URL: `/api/ratings`

### 1. Tạo Rating Mới

**Endpoint:** `POST /api/ratings`

**Headers:**

```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**

```json
{
  "eventId": "uuid",
  "collaboratorId": "uuid",
  "ratingScore": 4.5,
  "ratingComment": "Làm việc rất tốt, đúng giờ",
  "punctualityScore": 5.0,
  "qualityScore": 4.0,
  "attitudeScore": 4.5,
  "teamworkScore": 4.0
}
```

**Validation Rules:**

- `eventId`: Required, must exist in events table
- `collaboratorId`: Required, must exist in users table
- `ratingScore`: Required, 1.0 - 5.0
- `ratingComment`: Optional, max 1000 characters
- `punctualityScore`: Optional, 0.0 - 5.0
- `qualityScore`: Optional, 0.0 - 5.0
- `attitudeScore`: Optional, 0.0 - 5.0
- `teamworkScore`: Optional, 0.0 - 5.0

**Business Rules:**

- Chỉ BTC (người tạo event) mới có thể rating
- Mỗi BTC chỉ có thể rating một CTV một lần cho mỗi event
- Event phải có status = "COMPLETED"

**Response (201):**

```json
{
  "success": true,
  "message": "Rating created successfully",
  "data": {
    "ratingId": "uuid",
    "eventId": "uuid",
    "collaboratorId": "uuid",
    "ratedById": "uuid",
    "ratingScore": 4.5,
    "ratingComment": "Làm việc rất tốt, đúng giờ",
    "ratingDate": "2024-01-15T10:30:00Z",
    "punctualityScore": 5.0,
    "qualityScore": 4.0,
    "attitudeScore": 4.5,
    "teamworkScore": 4.0
  }
}
```

**Error Responses:**

- `400`: Validation error
- `401`: Unauthorized
- `403`: Forbidden (not event creator)
- `409`: Rating already exists
- `404`: Event or collaborator not found

### 2. Cập nhật Rating

**Endpoint:** `PUT /api/ratings/{ratingId}`

**Request Body:** (Same as create, all fields optional except ratingScore)

**Response (200):**

```json
{
  "success": true,
  "message": "Rating updated successfully",
  "data": {
    // Updated rating object
  }
}
```

### 3. Xóa Rating

**Endpoint:** `DELETE /api/ratings/{ratingId}`

**Response (200):**

```json
{
  "success": true,
  "message": "Rating deleted successfully"
}
```

### 4. Lấy Rating theo ID

**Endpoint:** `GET /api/ratings/{ratingId}`

**Response (200):**

```json
{
  "success": true,
  "message": "Rating retrieved successfully",
  "data": {
    "ratingId": "uuid",
    "eventId": "uuid",
    "eventTitle": "Event Name",
    "collaboratorId": "uuid",
    "collaboratorName": "Collaborator Name",
    "collaboratorEmail": "collaborator@email.com",
    "ratedById": "uuid",
    "ratedByName": "Rater Name",
    "ratingScore": 4.5,
    "ratingComment": "Comment",
    "ratingDate": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z",
    "punctualityScore": 5.0,
    "qualityScore": 4.0,
    "attitudeScore": 4.5,
    "teamworkScore": 4.0
  }
}
```

### 5. Lấy tất cả Rating của CTV

**Endpoint:** `GET /api/ratings/collaborator/{collaboratorId}`

**Query Parameters:**

- `page`: Optional, default 1
- `limit`: Optional, default 10
- `sortBy`: Optional, "ratingDate" | "ratingScore", default "ratingDate"
- `sortOrder`: Optional, "asc" | "desc", default "desc"

**Response (200):**

```json
{
  "success": true,
  "message": "Ratings retrieved successfully",
  "data": {
    "ratings": [
      // Array of rating objects
    ],
    "pagination": {
      "page": 1,
      "limit": 10,
      "total": 25,
      "totalPages": 3
    }
  }
}
```

### 6. Lấy tất cả Rating của Event

**Endpoint:** `GET /api/ratings/event/{eventId}`

**Response (200):**

```json
{
  "success": true,
  "message": "Event ratings retrieved successfully",
  "data": [
    // Array of rating objects
  ]
}
```

### 7. Lấy Rating cụ thể cho Event-Collaborator

**Endpoint:** `GET /api/ratings/event/{eventId}/collaborator/{collaboratorId}`

**Response (200):**

```json
{
  "success": true,
  "message": "Rating retrieved successfully",
  "data": {
    // Rating object or null if not found
  }
}
```

### 8. Lấy Rating Summary của CTV

**Endpoint:** `GET /api/ratings/collaborator/{collaboratorId}/summary`

**Response (200):**

```json
{
  "success": true,
  "message": "Rating summary retrieved successfully",
  "data": {
    "collaboratorId": "uuid",
    "collaboratorName": "Collaborator Name",
    "averageRating": 4.2,
    "totalRatings": 15,
    "ratingDistribution": {
      "5": 8,
      "4": 4,
      "3": 2,
      "2": 1,
      "1": 0
    },
    "averageScores": {
      "punctuality": 4.5,
      "quality": 4.0,
      "attitude": 4.3,
      "teamwork": 4.1
    },
    "recentRatings": [
      // Last 5 ratings
    ]
  }
}
```

### 9. **QUAN TRỌNG: Kiểm tra quyền Rating**

**Endpoint:** `GET /api/ratings/can-rate/event/{eventId}/collaborator/{collaboratorId}`

**Business Logic:**

```java
public boolean canRate(String eventId, String collaboratorId, String currentUserId) {
    // 1. Kiểm tra user có phải người tạo event không
    Event event = eventRepository.findById(eventId);
    if (!event.getCreatedBy().equals(currentUserId)) {
        return false;
    }

    // 2. Kiểm tra event có status COMPLETED không
    if (!"COMPLETED".equals(event.getStatus())) {
        return false;
    }

    // 3. Kiểm tra đã rating chưa
    boolean alreadyRated = ratingRepository.existsByEventIdAndCollaboratorIdAndRatedBy(
        eventId, collaboratorId, currentUserId
    );

    return !alreadyRated;
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Permission check completed",
  "data": true
}
```

### 10. Lấy Rating Summary của User (từ User Service)

**Endpoint:** `GET /api/users/{userId}/rating-summary`

**Response (200):**

```json
{
  "success": true,
  "message": "User rating summary retrieved successfully",
  "data": {
    // Same as endpoint 8
  }
}
```

---

## 🔐 Authentication & Authorization

### JWT Token Validation

- Tất cả endpoints yêu cầu valid JWT token
- Token phải chứa `userId` và `role`

### Permission Matrix

| Role          | Create Rating        | Update Rating         | Delete Rating         | View Ratings          |
| ------------- | -------------------- | --------------------- | --------------------- | --------------------- |
| EVENT_MANAGER | ✅ (own events only) | ✅ (own ratings only) | ✅ (own ratings only) | ✅ (own events only)  |
| ADMIN         | ✅ (all events)      | ✅ (all ratings)      | ✅ (all ratings)      | ✅ (all events)       |
| EMPLOYEE      | ❌                   | ❌                    | ❌                    | ✅ (own ratings only) |
| COLLABORATOR  | ❌                   | ❌                    | ❌                    | ✅ (own ratings only) |

---

## 🚨 Error Handling

### Standard Error Response Format

```json
{
  "success": false,
  "message": "Error description",
  "error": {
    "code": "ERROR_CODE",
    "details": "Additional error details"
  }
}
```

### Common Error Codes

- `VALIDATION_ERROR`: Input validation failed
- `UNAUTHORIZED`: Invalid or missing token
- `FORBIDDEN`: Insufficient permissions
- `NOT_FOUND`: Resource not found
- `DUPLICATE_RATING`: Rating already exists
- `EVENT_NOT_COMPLETED`: Event status is not COMPLETED
- `NOT_EVENT_CREATOR`: User is not the event creator

---

## 📊 Performance Considerations

### Database Optimization

1. **Indexes**: Đã tạo indexes cho các trường thường query
2. **Pagination**: Sử dụng pagination cho list endpoints
3. **Caching**: Cache rating summaries (Redis recommended)
4. **Batch Updates**: Sử dụng batch operations cho bulk updates

### Caching Strategy

```java
@Cacheable(value = "rating-summary", key = "#userId")
public RatingSummary getRatingSummary(String userId) {
    // Implementation
}

@CacheEvict(value = "rating-summary", key = "#collaboratorId")
public void evictRatingSummary(String collaboratorId) {
    // Called after rating create/update/delete
}
```

---

## 🧪 Testing Requirements

### Unit Tests

- [ ] Rating creation with valid data
- [ ] Rating creation with invalid data
- [ ] Permission checks
- [ ] Business rule validation
- [ ] Database constraints

### Integration Tests

- [ ] Full rating workflow
- [ ] Permission matrix testing
- [ ] Error handling
- [ ] Performance testing

### API Tests

```bash
# Test rating creation
curl -X POST /api/ratings \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "event-uuid",
    "collaboratorId": "collaborator-uuid",
    "ratingScore": 4.5,
    "ratingComment": "Great work!"
  }'

# Test permission check
curl -X GET /api/ratings/can-rate/event/{eventId}/collaborator/{collaboratorId} \
  -H "Authorization: Bearer {token}"
```

---

## 📝 Implementation Checklist

### Phase 1: Core Functionality

- [ ] Database schema creation
- [ ] Basic CRUD operations
- [ ] Permission validation
- [ ] Business rule enforcement

### Phase 2: Advanced Features

- [ ] Rating summary calculations
- [ ] Caching implementation
- [ ] Performance optimization
- [ ] Comprehensive testing

### Phase 3: Production Ready

- [ ] Error handling
- [ ] Logging
- [ ] Monitoring
- [ ] Documentation

---

## 🔗 Frontend Integration Points

### Service Layer (Frontend)

```typescript
// src/services/ratingService.ts
class RatingService {
  async createRating(data: CreateRatingRequest): Promise<Rating>;
  async updateRating(id: string, data: UpdateRatingRequest): Promise<Rating>;
  async deleteRating(id: string): Promise<void>;
  async canRate(eventId: string, collaboratorId: string): Promise<boolean>;
  async getRatingSummary(userId: string): Promise<RatingSummary>;
}
```

### Components (Frontend)

- `RatingForm`: Form để tạo/cập nhật rating
- `RatingDisplay`: Hiển thị rating
- `RatingSummary`: Thống kê rating
- `CollaboratorRatingCard`: Card rating cho CTV

---

## 📞 Support & Contact

Nếu có thắc mắc về specification này, vui lòng liên hệ:

- Frontend Team: [Contact Info]
- Backend Team: [Contact Info]

**Priority**: High - Tính năng rating là core feature của hệ thống
**Deadline**: [Specify deadline]
**Dependencies**: Event management system, User management system
