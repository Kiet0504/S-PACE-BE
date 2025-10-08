#!/bin/bash

# Cloudflare Pro Setup Script for S-PACE Project
set -e

echo "�� Setting up Cloudflare Pro for S-PACE Project..."

# Check if Cloudflare CLI is installed
if ! command -v cloudflared &> /dev/null; then
    echo "�� Installing Cloudflare CLI..."
    wget -q https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb
    sudo dpkg -i cloudflared-linux-amd64.deb
    rm cloudflared-linux-amd64.deb
fi

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "�� Installing jq..."
    sudo apt update && sudo apt install -y jq
fi

# Function to get Cloudflare API token
get_api_token() {
    echo "🔑 Please enter your Cloudflare API Token:"
    echo "   (Get it from: https://dash.cloudflare.com/profile/api-tokens)"
    echo "   Required permissions: Zone:Read, DNS:Edit, SSL:Edit, Cache:Edit"
    read -s CF_API_TOKEN
    export CF_API_TOKEN
}

# Function to get zone ID
get_zone_id() {
    echo "�� Please enter your domain name (e.g., example.com):"
    read DOMAIN_NAME
    export DOMAIN_NAME
    
    echo "�� Getting Zone ID for $DOMAIN_NAME..."
    ZONE_ID=$(curl -s -X GET "https://api.cloudflare.com/client/v4/zones?name=$DOMAIN_NAME" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" | jq -r '.result[0].id')
    
    if [ "$ZONE_ID" = "null" ] || [ -z "$ZONE_ID" ]; then
        echo "❌ Error: Could not find zone for $DOMAIN_NAME"
        exit 1
    fi
    
    echo "✅ Zone ID: $ZONE_ID"
    export ZONE_ID
}

# Function to configure DNS
configure_dns() {
    echo "�� Configuring DNS records..."
    
    # Get VPS IP
    echo "🌐 Please enter your VPS IP address:"
    read VPS_IP
    
    # Create A record
    curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data "{
            \"type\": \"A\",
            \"name\": \"@\",
            \"content\": \"$VPS_IP\",
            \"proxied\": true
        }" | jq '.success'
    
    # Create CNAME record for www
    curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data "{
            \"type\": \"CNAME\",
            \"name\": \"www\",
            \"content\": \"$DOMAIN_NAME\",
            \"proxied\": true
        }" | jq '.success'
    
    echo "✅ DNS records configured"
}

# Function to configure SSL/TLS
configure_ssl() {
    echo "🔒 Configuring SSL/TLS settings..."
    
    # Set encryption mode to Full (strict)
    curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/ssl" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data '{"value":"full"}' | jq '.success'
    
    # Enable Always Use HTTPS
    curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/always_use_https" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data '{"value":"on"}' | jq '.success'
    
    # Enable HSTS
    curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/security_header" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data '{"value":{"enabled":true,"max_age":31536000,"include_subdomains":true,"nosniff":true}}' | jq '.success'
    
    echo "✅ SSL/TLS configured"
}

# Function to configure caching
configure_caching() {
    echo "💾 Configuring caching settings..."
    
    # Set cache level to aggressive
    curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/cache_level" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data '{"value":"aggressive"}' | jq '.success'
    
    # Enable Brotli compression
    curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/brotli" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data '{"value":"on"}' | jq '.success'
    
    echo "✅ Caching configured"
}

# Function to configure security
configure_security() {
    echo "��️ Configuring security settings..."
    
    # Enable WAF
    curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/waf" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data '{"value":"on"}' | jq '.success'
    
    # Set security level to high
    curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/security_level" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data '{"value":"high"}' | jq '.success'
    
    # Enable Bot Fight Mode
    curl -s -X PATCH "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/settings/bot_fight_mode" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data '{"value":"on"}' | jq '.success'
    
    echo "✅ Security configured"
}

# Function to create page rules
create_page_rules() {
    echo "📋 Creating page rules..."
    
    # API endpoints - bypass cache
    curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/pagerules" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data "{
            \"targets\": [{\"target\": \"url\", \"constraint\": {\"operator\": \"matches\", \"value\": \"$DOMAIN_NAME/api/*\"}}],
            \"actions\": [{\"id\": \"cache_level\", \"value\": \"bypass\"}],
            \"priority\": 1,
            \"status\": \"active\"
        }" | jq '.success'
    
    # Actuator endpoints - bypass cache
    curl -s -X POST "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/pagerules" \
        -H "Authorization: Bearer $CF_API_TOKEN" \
        -H "Content-Type: application/json" \
        --data "{
            \"targets\": [{\"target\": \"url\", \"constraint\": {\"operator\": \"matches\", \"value\": \"$DOMAIN_NAME/actuator/*\"}}],
            \"actions\": [{\"id\": \"cache_level\", \"value\": \"bypass\"}],
            \"priority\": 2,
            \"status\": \"active\"
        }" | jq '.success'
    
    echo "✅ Page rules created"
}

# Main execution
main() {
    echo "================================================"
    echo "🌐 Cloudflare Pro Setup for S-PACE Project"
    echo "================================================"
    
    get_api_token
    get_zone_id
    configure_dns
    configure_ssl
    configure_caching
    configure_security
    create_page_rules
    
    echo ""
    echo "✅ Cloudflare Pro setup completed!"
    echo "🌐 Your domain: https://$DOMAIN_NAME"
    echo "�� Dashboard: https://dash.cloudflare.com"
    echo ""
    echo "🔧 Next steps:"
    echo "1. Update your nginx.conf with your domain name"
    echo "2. Restart your Docker containers"
    echo "3. Test your application"
}

# Run main function
main
