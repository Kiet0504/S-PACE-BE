#!/bin/bash

# Bash script to debug avatar upload issue specifically
echo "=== Avatar Upload Debug ==="

echo "1. Checking current database configuration:"
grep "DB_HOST" .env

echo ""
echo "2. Checking storage configuration:"
grep "STORAGE_TYPE" .env

echo ""
echo "3. Checking AWS S3 configuration:"
grep "AWS_" .env

echo ""
echo "4. Testing avatar upload endpoint:"
echo "   Testing OPTIONS request to avatar endpoint:"
curl -X OPTIONS \
  -H "Origin: https://s-pace.com.vn" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: authorization,content-type" \
  -v \
  https://api.s-pace.com.vn/api/users/my-profile/avatar 2>&1 | grep -i "access-control\|cors" || echo "   No CORS headers found"

echo ""
echo "5. Checking application logs for avatar upload errors:"
if [ -f "logs/space-app.log" ]; then
    echo "   Recent avatar upload errors:"
    grep -i "avatar\|upload\|file" logs/space-app.log | grep -i "error\|exception\|failed" | tail -10 || echo "   No avatar upload errors found"
fi

echo ""
echo "6. Checking if uploads directory exists:"
if [ -d "uploads/avatars" ]; then
    echo "   ✅ uploads/avatars directory exists"
    ls -la uploads/avatars/
else
    echo "   ❌ uploads/avatars directory does not exist"
    echo "   Creating directory..."
    mkdir -p uploads/avatars
    echo "   ✅ Created uploads/avatars directory"
fi

echo ""
echo "7. Checking file permissions:"
ls -la uploads/
ls -la uploads/avatars/ 2>/dev/null || echo "   Directory not accessible"

echo ""
echo "8. Recommendations:"
echo "   - Keep DB_HOST=postgres (it works for other APIs)"
echo "   - Focus on fixing avatar upload specifically"
echo "   - Check if it's a file upload size limit issue"
echo "   - Check if it's a storage configuration issue"
echo "   - Check if it's a permissions issue"
