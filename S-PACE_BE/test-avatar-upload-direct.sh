#!/bin/bash

# Test avatar upload directly
echo "=== Testing Avatar Upload ==="

# Create a test image file
echo "Creating test image file..."
echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==" | base64 -d > test-avatar.png

echo "Test image created: test-avatar.png"
ls -la test-avatar.png

echo ""
echo "Testing avatar upload endpoint..."

# Test with a real user ID (you'll need to replace with actual user ID)
USER_ID="289e7da4-383d-4420-a96f-ba7b221c2478"

echo "Testing POST to /api/users/$USER_ID/avatar"
curl -X POST \
  -H "Origin: https://s-pace.com.vn" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@test-avatar.png" \
  -v \
  https://api.s-pace.com.vn/api/users/$USER_ID/avatar 2>&1

echo ""
echo "Testing OPTIONS request:"
curl -X OPTIONS \
  -H "Origin: https://s-pace.com.vn" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: authorization,content-type" \
  -v \
  https://api.s-pace.com.vn/api/users/$USER_ID/avatar 2>&1

echo ""
echo "Cleaning up test file..."
rm -f test-avatar.png

echo ""
echo "=== Test Complete ==="
