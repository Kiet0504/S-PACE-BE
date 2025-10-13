#!/bin/bash

# SSL Setup using Webroot Method (No DNS challenge needed)
# This method is simpler and doesn't require DNS TXT records

echo "🔧 Setting up SSL Certificate using Webroot Method"
echo "=================================================="

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   echo "❌ Please run as root: sudo ./ssl-setup-webroot.sh"
   exit 1
fi

# Install nginx and certbot if needed
if ! command -v nginx &> /dev/null; then
    echo "📦 Installing nginx..."
    apt update && apt install -y nginx
fi

if ! command -v certbot &> /dev/null; then
    echo "📦 Installing certbot..."
    apt install -y certbot python3-certbot-nginx
fi

# Create webroot directory
echo "📁 Creating webroot directory..."
mkdir -p /var/www/html
chown -R www-data:www-data /var/www/html

# Create a simple index.html for validation
cat > /var/www/html/index.html << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>S-PACE SSL Validation</title>
</head>
<body>
    <h1>S-PACE</h1>
    <p>SSL Certificate Validation Page</p>
</body>
</html>
EOF

# Create basic nginx config for webroot validation
cat > /etc/nginx/sites-available/ssl-validation << 'EOF'
server {
    listen 80;
    server_name s-pace.com.vn www.s-pace.com.vn api.s-pace.com.vn;
    
    root /var/www/html;
    index index.html;
    
    location / {
        try_files $uri $uri/ =404;
    }
    
    # Allow Let's Encrypt validation
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }
}
EOF

# Enable the site
ln -sf /etc/nginx/sites-available/ssl-validation /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default

# Test and start nginx
nginx -t && systemctl restart nginx

# Wait a moment for nginx to start
sleep 3

# Get SSL certificate using webroot method
echo "🔐 Getting SSL certificate using webroot method..."
certbot certonly --webroot -w /var/www/html \
    -d "s-pace.com.vn" \
    -d "www.s-pace.com.vn" \
    -d "api.s-pace.com.vn" \
    --non-interactive --agree-tos --email admin@s-pace.com.vn

# Check if certificate was created successfully
if [[ -f "/etc/letsencrypt/live/s-pace.com.vn/fullchain.pem" ]]; then
    echo "✅ Certificate created successfully!"
    
    # Copy certificates to nginx directory
    echo "📋 Copying certificates..."
    mkdir -p /etc/nginx/ssl
    cp /etc/letsencrypt/live/s-pace.com.vn/fullchain.pem /etc/nginx/ssl/
    cp /etc/letsencrypt/live/s-pace.com.vn/privkey.pem /etc/nginx/ssl/
    chmod 644 /etc/nginx/ssl/fullchain.pem
    chmod 600 /etc/nginx/ssl/privkey.pem
    
    # Create HTTPS nginx config
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

    # Enable HTTPS config
    ln -sf /etc/nginx/sites-available/s-pace-https /etc/nginx/sites-enabled/
    rm -f /etc/nginx/sites-enabled/ssl-validation
    
    # Test and reload nginx
    if nginx -t; then
        echo "✅ Nginx configuration is valid"
        systemctl reload nginx
        echo "🔄 Nginx reloaded successfully"
    else
        echo "❌ Nginx configuration has errors"
        exit 1
    fi
    
    # Setup auto-renewal
    echo "⏰ Setting up auto-renewal..."
    cat > /etc/cron.d/certbot-renew << 'EOF'
# Auto-renew Let's Encrypt certificates
0 2 * * * root /usr/bin/certbot renew --quiet --renew-hook "systemctl reload nginx"
EOF
    
    echo ""
    echo "🎉 SSL Certificate setup completed successfully!"
    echo ""
    echo "✅ You can now access:"
    echo "   - Main site: https://s-pace.com.vn"
    echo "   - API: https://api.s-pace.com.vn"
    echo "   - Swagger UI: https://api.s-pace.com.vn/swagger-ui/index.html"
    echo ""
    echo "🔍 Test your setup:"
    echo "   curl -I https://api.s-pace.com.vn/actuator/health"
    echo ""
    
else
    echo "❌ Certificate creation failed"
    echo "Please check the error messages above and try again"
    exit 1
fi
