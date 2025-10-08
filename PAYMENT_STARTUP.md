# 🚀 Payment System Startup Guide

## Quick Start Checklist

### ✅ Step 1: Start Application
```bash
mvn spring-boot:run
```
**Wait for:** `Started SPaceApplication`

### ✅ Step 2: Start ngrok
```bash
ngrok http 8080
```
**Copy new URL:** `https://xxxxxx.ngrok-free.app`

### ✅ Step 3: Update PayOS Webhook
1. Login to PayOS Dashboard
2. Go to Settings > Webhook
3. Update URL: `https://xxxxxx.ngrok-free.app/api/webhooks/payos-payment`
4. Save changes

### ✅ Step 4: Test Webhook
```bash
curl https://xxxxxx.ngrok-free.app/api/webhooks/payos-payment
```
**Expected:** `PayOS webhook endpoint is working! Ready to receive payments.`

### ✅ Step 5: Test Payment API
```bash
curl -X POST http://localhost:8080/api/payments/create-link \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Test Product",
    "description": "Test payment",
    "price": 10000,
    "returnUrl": "https://example.com/success",
    "cancelUrl": "https://example.com/cancel"
  }'
```

## 🔧 Troubleshooting

### If application won't start:
```bash
# Check port 8080
netstat -an | findstr :8080

# Kill process if needed
taskkill /F /PID [PID_NUMBER]
```

### If PayOS credentials missing:
- Check `application.yml` has test credentials:
```yaml
payos:
  client-id: ${PAYOS_CLIENT_ID:test-client-id}
  api-key: ${PAYOS_API_KEY:test-api-key}
  checksum-key: ${PAYOS_CHECKSUM_KEY:test-checksum-key}
```

### Test URLs:
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **Webhook URL:** http://localhost:8080/api/payments/webhook-url
- **Health Check:** http://localhost:8080/actuator/health

## 🎯 Production Notes

For production deployment:
1. Replace test PayOS credentials with real ones
2. Use domain name instead of ngrok
3. Set up SSL certificate
4. Configure proper CORS origins