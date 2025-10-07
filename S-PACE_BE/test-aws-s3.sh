#!/bin/bash

# Test AWS S3 connection and permissions
echo "=== Testing AWS S3 Connection ==="

# Load environment variables
source .env

echo "1. AWS Configuration:"
echo "   Region: $AWS_REGION"
echo "   Bucket: $AWS_S3_BUCKET_NAME"
echo "   Access Key: ${AWS_ACCESS_KEY_ID:0:8}..."

echo ""
echo "2. Testing AWS S3 connection with AWS CLI:"
if command -v aws &> /dev/null; then
    echo "   AWS CLI found, testing connection..."
    
    # Test S3 access
    aws s3 ls s3://$AWS_S3_BUCKET_NAME --region $AWS_REGION 2>&1
    
    echo ""
    echo "   Testing S3 bucket permissions..."
    aws s3api get-bucket-location --bucket $AWS_S3_BUCKET_NAME --region $AWS_REGION 2>&1
    
    echo ""
    echo "   Testing S3 bucket policy..."
    aws s3api get-bucket-policy --bucket $AWS_S3_BUCKET_NAME --region $AWS_REGION 2>&1 || echo "   No bucket policy found"
    
else
    echo "   AWS CLI not found, testing with curl..."
    
    # Test S3 endpoint
    echo "   Testing S3 endpoint accessibility..."
    curl -I https://$AWS_S3_BUCKET_NAME.s3.$AWS_REGION.amazonaws.com/ 2>&1
    
    echo ""
    echo "   Testing S3 public URL..."
    curl -I $AWS_S3_PUBLIC_URL/ 2>&1
fi

echo ""
echo "3. Common AWS S3 issues:"
echo "   - Invalid access key/secret key"
echo "   - Bucket doesn't exist"
echo "   - Region mismatch"
echo "   - Bucket permissions (public read/write)"
echo "   - CORS configuration on S3 bucket"

echo ""
echo "4. S3 Bucket CORS Configuration (if needed):"
echo '   {
     "CORSRules": [
       {
         "AllowedHeaders": ["*"],
         "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
         "AllowedOrigins": ["https://s-pace.com.vn", "https://api.s-pace.com.vn"],
         "ExposeHeaders": ["ETag"],
         "MaxAgeSeconds": 3000
       }
     ]
   }'

echo ""
echo "5. Recommendations:"
echo "   - Verify AWS credentials are correct"
echo "   - Check S3 bucket exists and is accessible"
echo "   - Verify S3 bucket CORS configuration"
echo "   - Check S3 bucket permissions"
