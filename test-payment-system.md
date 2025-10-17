# 🧪 HƯỚNG DẪN TEST PAYMENT SYSTEM

## ✅ CHECKLIST HOÀN THÀNH

### 1. Code đã copy:
- ✅ Controllers (Payment, PaymentWebhook)
- ✅ DTOs (PayOsRequest, PayOsResponse, WebhookResponse, etc.)
- ✅ Entities (SubscriptionPlan, UserSubscription, SubscriptionPayment)
- ✅ Repositories
- ✅ Services (PaymentService, SubscriptionService)
- ✅ Config (PayOSConfig)
- ✅ Database migrations
- ✅ Dependencies trong pom.xml
- ✅ PayOS config trong application.yml
- ✅ Logging configuration
- ✅ ObjectMapper bean

---

## 📋 BƯỚC TIẾP THEO

### BƯỚC 1: Build và kiểm tra lỗi compile

```bash
# Clean và compile
mvn clean compile

# Kiểm tra có lỗi không
mvn test-compile
```

**Nếu có lỗi:**
- Missing imports → Kiểm tra lại dependencies
- Class not found → Kiểm tra lại package name
- Bean not found → Kiểm tra @Component, @Service annotations

---

### BƯỚC 2: Kiểm tra Database Migration

```bash
# Xem danh sách migrations
mvn flyway:info

# Chạy migrations (nếu chưa chạy)
mvn flyway:migrate
```

**Kiểm tra trong database:**
```sql
-- Kiểm tra bảng đã tạo chưa
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('subscription_plan', 'user_subscription', 'subscription_payment');

-- Kiểm tra data mẫu
SELECT * FROM subscription_plan;
```

**Kết quả mong đợi:**
```
plan_id                                  | name  | price    | max_recruitment_limit
-----------------------------------------|-------|----------|----------------------
xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx    | Free  | 0.00     | 5
xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx    | Basic | 199000   | 50
xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx    | Pro   | 299000   | 100
```

---

### BƯỚC 3: Start Application

```bash
# Start với dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Hoặc
java -jar target/your-app.jar --spring.profiles.active=dev
```

**Kiểm tra logs:**
```
✅ PayOSConfig initialized successfully
✅ SecurityFilterChain configured
✅ Flyway migration completed
✅ Application started on port 8080
```

---

### BƯỚC 4: Test Endpoints Cơ Bản

#### 4.1. Test Health Check
```bash
curl http://localhost:8080/actuator/health
```
**Kết quả mong đợi:** `{"status":"UP"}`

#### 4.2. Test Webhook Endpoint
```bash
curl http://localhost:8080/api/webhooks/payos-payment
```
**Kết quả mong đợi:** `"PayOS webhook endpoint is working! Ready to receive payments."`

#### 4.3. Test Lấy danh sách Plans
```bash
curl http://localhost:8080/api/subscriptions/plans
```
**Kết quả mong đợi:** JSON array của plans

---

### BƯỚC 5: Test Payment Flow (Cần JWT Token)

#### 5.1. Login để lấy JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "your-email@example.com",
    "password": "your-password"
  }'
```
**Lưu access_token từ response**

#### 5.2. Tạo Payment Link
```bash
curl -X POST http://localhost:8080/api/subscriptions/purchase-with-payment \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "planId": "PLAN_UUID_FROM_DATABASE",
    "returnUrl": "http://localhost:3000/payment/success",
    "cancelUrl": "http://localhost:3000/payment/cancel"
  }'
```

**Kết quả mong đợi:**
```json
{
  "paymentUrl": "https://pay.payos.vn/web/...",
  "orderCode": REDACTED_PASSWORD67890,
  "message": "Payment link created successfully"
}
```

#### 5.3. Test Payment
1. Mở `paymentUrl` trong browser
2. Sử dụng test card PayOS:
   - Card: `9704 0000 0000 0018`
   - Name: `NGUYEN VAN A`
   - Date: `03/07`
   - OTP: `REDACTED_PASSWORD6`

---

### BƯỚC 6: Test Webhook (Cần Ngrok)

#### 6.1. Cài đặt Ngrok
```bash
# Windows
choco install ngrok

# Hoặc download từ https://ngrok.com/download
```

#### 6.2. Chạy Ngrok
```bash
ngrok http 8080
```
**Lưu lại URL:** `https://xxxx-xxxx-xxxx.ngrok-free.app`

#### 6.3. Cập nhật Webhook URL trên PayOS
1. Login: https://my.payos.vn/
2. Vào **Cài đặt** → **Webhook**
3. URL: `https://xxxx-xxxx-xxxx.ngrok-free.app/api/webhooks/payos-payment`
4. **Lưu**

#### 6.4. Test Webhook
Sau khi thanh toán thành công, kiểm tra logs:
```
✅ Received PayOS webhook - Body: {...}, Signature: xxx
✅ Successfully processed payment webhook for orderCode: REDACTED_PASSWORD6
✅ Successfully confirmed subscription payment for orderCode: REDACTED_PASSWORD6
```

---

### BƯỚC 7: Kiểm Tra Database Sau Payment

```sql
-- Kiểm tra payment record
SELECT * FROM subscription_payment 
WHERE status = 'COMPLETED' 
ORDER BY created_at DESC 
LIMIT 5;

-- Kiểm tra user subscription
SELECT us.*, sp.name, sp.price 
FROM user_subscription us
JOIN subscription_plan sp ON us.plan_id = sp.plan_id
ORDER BY us.purchased_at DESC
LIMIT 5;
```

---

## 🐛 TROUBLESHOOTING

### Lỗi: "PayOSConfig bean not found"
**Giải pháp:** Kiểm tra @Configuration annotation trong PayOSConfig.java

### Lỗi: "Table subscription_plan doesn't exist"
**Giải pháp:** 
```bash
mvn flyway:migrate
```

### Lỗi: "Invalid signature" từ PayOS webhook
**Giải pháp:** Kiểm tra PAYOS_CHECKSUM_KEY trong application.yml

### Lỗi: "Access Denied" khi gọi webhook
**Giải pháp:** Kiểm tra SecurityConfig đã permitAll cho `/api/webhooks/**`

### Webhook không nhận được call từ PayOS
**Giải pháp:**
1. Kiểm tra Ngrok đang chạy
2. Kiểm tra webhook URL trên PayOS dashboard
3. Test manual webhook:
```bash
curl -X POST http://localhost:8080/api/webhooks/payos-payment \
  -H "Content-Type: application/json" \
  -d '{"test": "data"}'
```

---

## 📊 MONITORING & LOGS

### Xem Payment Logs
```bash
# Windows PowerShell
Get-Content logs/space-app.log -Tail 50 -Wait | Select-String "Payment"

# Hoặc
type logs\space-app.log | findstr /I "payment webhook"
```

### Xem Real-time Logs
```bash
tail -f logs/space-app.log | grep -i payment
```

---

## 🎯 NEXT STEPS

Sau khi test thành công local:

1. **Deploy lên VPS/Server**
   - Cập nhật PayOS webhook URL thật
   - Sử dụng domain thật (không dùng ngrok)
   - Enable HTTPS

2. **Security Enhancements**
   - Verify PayOS signature trong webhook
   - Add rate limiting
   - Log tất cả payment transactions

3. **Business Logic**
   - Send email confirmation sau payment
   - Implement subscription expiry logic
   - Add payment refund functionality

4. **Frontend Integration**
   - Tạo UI cho plan selection
   - Payment success/failure pages
   - User subscription dashboard

---

## 📝 NOTES

- **Test Mode:** PayOS tự động ở test mode nếu dùng test credentials
- **Production:** Đổi credentials thật trên PayOS dashboard
- **Webhook Retry:** PayOS sẽ retry webhook 3 lần nếu fail
- **Timeout:** Payment link có hiệu lực 15 phút

---

## ✅ DEPLOYMENT CHECKLIST

Khi migrate sang dự án mới:
- [ ] Copy tất cả files theo checklist
- [ ] Update package names nếu khác
- [ ] Run `mvn clean install`
- [ ] Run Flyway migrations
- [ ] Insert sample data vào subscription_plan
- [ ] Test all endpoints
- [ ] Configure webhook URL
- [ ] Test payment flow end-to-end

