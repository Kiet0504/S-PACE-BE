#!/bin/bash

# Setup SSL after certificate is created
# Run this script on your server after certbot has created the certificate

echo "🔧 Setting up SSL after certificate creation"
echo "============================================="

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   echo "❌ Please run as root: sudo ./setup-ssl-after-cert.sh"
   exit 1
fi

# Check if certificate files exist
if [[ ! -f "/etc/letsencrypt/live/s-pace.com.vn/fullchain.pem" ]]; then
    echo "❌ Certificate files not found. Please run certbot first."
    exit 1
fi

echo "✅ Certificate files found"

# Copy certificates to nginx directory
echo "📋 Copying certificates to nginx directory..."
mkdir -p /etc/nginx/ssl
cp /etc/letsencrypt/live/s-pace.com.vn/fullchain.pem /etc/nginx/ssl/
cp /etc/letsencrypt/live/s-pace.com.vn/privkey.pem /etc/nginx/ssl/

# Set proper permissions
chmod 644 /etc/nginx/ssl/fullchain.pem
chmod 600 /etc/nginx/ssl/privkey.pem
chown root:root /etc/nginx/ssl/*.pem

echo "✅ Certificates copied successfully"

# Create HTTPS nginx configuration
echo "🔧 Creating HTTPS nginx configuration..."
cat > /etc/nginx/sites-available/s-pace-https << 'EOF'
# HTTP to HTTPS redirect
server {
    listen 80;
    server_name s-pace.com.vn www.s-pace.com.vn api.s-pace.com.vn;
    return 301 https://$server_name$request_uri;
}

# Main domain HTTPS
server {
    listen 443 ssl http2;
    server_name s-pace.com.vn www.s-pace.com.vn;

    # SSL Configuration
    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # Security headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    client_max_body_size 10M;
}

# API subdomain HTTPS
server {
    listen 443 ssl http2;
    server_name api.s-pace.com.vn;

    # SSL Configuration
    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # Security headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    # API endpoints
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    client_max_body_size 10M;
}
EOF

echo "✅ Nginx configuration created"

# Enable HTTPS configuration
echo "🔗 Enabling HTTPS configuration..."
ln -sf /etc/nginx/sites-available/s-pace-https /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default

# Test nginx configuration
echo "🔍 Testing nginx configuration..."
if nginx -t; then
    echo "✅ Nginx configuration is valid"
    
    # Reload nginx
    echo "🔄 Reloading nginx..."
    systemctl reload nginx
    echo "✅ Nginx reloaded successfully"
else
    echo "❌ Nginx configuration has errors"
    echo "Please check the configuration and try again"
    exit 1
fi

# Setup auto-renewal (for manual certificates, we need a different approach)
echo "⏰ Setting up certificate renewal reminder..."
cat > /etc/cron.d/certbot-manual-renewal << 'EOF'
# Manual certificate renewal reminder
# This certificate was created with --manual flag and needs manual renewal
# Certificate expires on 2026-01-04
# Run this command before expiry: certbot certonly --manual --preferred-challenges dns -d "s-pace.com.vn" -d "www.s-pace.com.vn" -d "api.s-pace.com.vn"
0 2 1 1 * root echo "SSL Certificate expires soon! Please renew manually." | mail -s "SSL Certificate Renewal Reminder" admin@s-pace.com.vn
EOF

echo ""
echo "🎉 SSL setup completed successfully!"
echo ""
echo "✅ Your SSL certificate is now active for:"
echo "   - s-pace.com.vn"
echo "   - www.s-pace.com.vn"
echo "   - api.s-pace.com.vn"
echo ""
echo "🌐 Test your setup:"
echo "   curl -I https://s-pace.com.vn"
echo "   curl -I https://api.s-pace.com.vn/actuator/health"
echo "   curl -I https://api.s-pace.com.vn/swagger-ui/index.html"
echo ""
echo "⚠️  Important: This certificate was created with --manual flag"
echo "   You need to renew it manually before 2026-01-04"
echo "   Use the same certbot command to renew"
echo ""
