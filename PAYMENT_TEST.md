# PayOS Subscription Payment Testing

## Your PayOS Credentials
- Client ID: e370ec77-6573-4fbb-951a-75f21d0d5339
- API Key: 0734a1ad-6a0f-4cb7-b23b-b7a6458c001e
- Checksum Key: 5ef46132587dac428abe9d10d0bf91a3f90c0239f332b2adbabe759293bfec4e

## Issue Resolution
The 500 error you're experiencing is likely due to:

1. **The running application instances are using old configuration**
   - The applications running on port 8080 still have the old "test-client-id" credentials
   - Spring DevTools should auto-reload, but it's not happening

## Quick Fix

### Option 1: Restart the Application
Stop all Java processes and restart fresh:
```bash
# Kill all Java processes (Windows)
taskkill /F /IM java.exe

# Start the application
mvn spring-boot:run
```

### Option 2: Use a Different Port
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=8090"
```

## Test Your API

### Endpoint
```
POST http://localhost:8080/api/subscriptions/purchase-with-payment
```

### Headers
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

### Request Body
```json
{
  "planId": "c15fc2ec-40c4-4d79-bf2c-89b63425abda",
  "returnUrl": "http://localhost:3000/subscription/success",
  "cancelUrl": "http://localhost:3000/subscription/cancel",
  "description": "Upgrade to Basic Plan"
}
```

## Expected Response
```json
{
  "success": true,
  "message": "Payment link created successfully",
  "data": {
    "bin": "970405",
    "accountNumber": "KIENNGUYEN",
    "accountName": "KIEN NGUYEN",
    "amount": 199000,
    "description": "Upgrade to Basic Plan",
    "orderCode": REDACTED_PASSWORD6789,
    "currency": "VND",
    "paymentLinkId": "abc123",
    "status": "PENDING",
    "checkoutUrl": "https://pay.payos.vn/web/abc123",
    "qrCode": "data:image/png;base64,..."
  }
}
```

## Troubleshooting

### If you still get a 500 error:

1. **Check if credentials are loaded correctly**
   Look for these log lines when the app starts:
   ```
   INFO  c.e.S.config.PayOSConfig - Initializing PayOS with clientId: e370ec77-6573-4fbb-951a-75f21d0d5339
   INFO  c.e.S.config.PayOSConfig - API Key length: 36
   INFO  c.e.S.config.PayOSConfig - Checksum Key length: 64
   ```

2. **Verify PayOS Account**
   - Login to https://my.payos.vn/
   - Check if your account is active
   - Verify the credentials match

3. **Check the actual error**
   The 500 error might be from PayOS API itself. Common causes:
   - Account not activated
   - Test mode vs Production mode mismatch
   - Payment channel not configured
   - Invalid plan price (must be minimum 10,000 VND)

## Configuration Applied
Your `application.yml` has been updated with:
```yaml
payos:
  client-id: ${PAYOS_CLIENT_ID:e370ec77-6573-4fbb-951a-75f21d0d5339}
  api-key: ${PAYOS_API_KEY:0734a1ad-6a0f-4cb7-b23b-b7a6458c001e}
  checksum-key: ${PAYOS_CHECKSUM_KEY:5ef46132587dac428abe9d10d0bf91a3f90c0239f332b2adbabe759293bfec4e}
```

The credentials are now hardcoded as default values, so they will be used even without environment variables.