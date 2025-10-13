#!/bin/bash

# Check file upload limits and configuration
echo "=== Checking Upload Limits ==="

echo "1. Checking application.yml for file upload limits:"
grep -A 10 -B 5 "multipart\|file\|upload" src/main/resources/application.yml

echo ""
echo "2. Checking application-prod.yml for file upload limits:"
grep -A 10 -B 5 "multipart\|file\|upload" src/main/resources/application-prod.yml

echo ""
echo "3. Checking if there are any file size limits in code:"
grep -r "max-file-size\|max-request-size\|file-size" src/ || echo "No file size limits found in code"

echo ""
echo "4. Checking AWS S3 configuration for file size limits:"
echo "   AWS S3 single upload limit: 5GB"
echo "   AWS S3 multipart upload limit: 5TB"

echo ""
echo "5. Checking if there are any proxy/nginx limits:"
echo "   Check nginx configuration for client_max_body_size"
echo "   Check if there's a reverse proxy with upload limits"

echo ""
echo "6. Common file upload issues:"
echo "   - File size too large"
echo "   - File type not allowed"
echo "   - Missing authorization header"
echo "   - AWS credentials issue"
echo "   - Network timeout"

echo ""
echo "7. Recommendations:"
echo "   - Check nginx/load balancer configuration"
echo "   - Verify AWS S3 bucket permissions"
echo "   - Test with smaller file size"
echo "   - Check if authorization header is required"
