#!/bin/bash

# S-PACE VPS Deployment Script
set -e

echo "�� Starting S-PACE VPS Deployment..."

# Update system
echo "📦 Updating system packages..."
sudo apt update && sudo apt upgrade -y

# Install Docker
echo "🐳 Installing Docker..."
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
fi

# Install Docker Compose
echo "�� Installing Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

# Create necessary directories
echo "📁 Creating directories..."
mkdir -p uploads/cvs uploads/certificates uploads/avatars logs ssl

# Set permissions
echo "�� Setting permissions..."
chmod 755 uploads uploads/cvs uploads/certificates uploads/avatars logs

# Copy environment file
echo "⚙️ Setting up environment..."
if [ ! -f .env ]; then
    cp .env.prod .env
    echo "⚠️  Please edit .env file with your production values!"
fi

# Build and start services
echo "��️ Building and starting services..."
docker-compose -f docker-compose-prod.yml down
docker-compose -f docker-compose-prod.yml build --no-cache
docker-compose -f docker-compose-prod.yml up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Check health
echo "�� Checking service health..."
docker-compose -f docker-compose-prod.yml ps

echo "✅ Deployment completed!"
echo "🌐 Your application should be available at:"
echo "   - HTTP: http://your-server-ip"
echo "   - Health: http://your-server-ip/actuator/health"
echo "   - API Docs: http://your-server-ip/swagger-ui.html"
