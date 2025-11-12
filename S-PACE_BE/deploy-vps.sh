#!/bin/bash

# S-PACE VPS Deployment Script
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check if we're in the right directory
check_project_directory() {
    if [ ! -f "docker-compose-prod.yml" ]; then
        print_error "docker-compose-prod.yml not found!"
        print_error "Please run this script from the S-PACE_BE directory"
        exit 1
    fi
}

# Function to check Git status
check_git_status() {
    if [ -n "$(git status --porcelain)" ]; then
        print_warning "You have uncommitted changes!"
        echo "Current changes:"
        git status --short
        read -p "Do you want to continue? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_error "Deployment cancelled"
            exit 1
        fi
    fi
}

# Function to check SSL certificate
check_ssl_certificate() {
    print_status "Checking SSL certificate..."
    
    if [ ! -f "ssl/fullchain.pem" ] || [ ! -f "ssl/privkey.pem" ]; then
        print_warning "SSL certificate files not found!"
        print_warning "Please ensure you have:"
        print_warning "  - ssl/fullchain.pem"
        print_warning "  - ssl/privkey.pem"
        print_warning "You can obtain SSL certificates using Let's Encrypt or your certificate provider."
        print_warning "For api.s-pace.com.vn subdomain, make sure your certificate includes:"
        print_warning "  - s-pace.com.vn"
        print_warning "  - www.s-pace.com.vn" 
        print_warning "  - api.s-pace.com.vn"
        read -p "Do you want to continue without SSL? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_error "Deployment cancelled - SSL certificate required"
            exit 1
        fi
    else
        print_success "SSL certificate files found"
        
        # Check if certificate includes api.s-pace.com.vn
        if openssl x509 -in ssl/fullchain.pem -text -noout | grep -q "api.s-pace.com.vn"; then
            print_success "Certificate includes api.s-pace.com.vn subdomain"
        else
            print_warning "Certificate may not include api.s-pace.com.vn subdomain"
            print_warning "Please verify your certificate includes all required domains"
        fi
    fi
}

# Function to wait for service health
wait_for_health() {
    local max_attempts=60  # Increased from 30 to 60 (2 minutes total)
    local attempt=1
    
    print_status "Waiting for application to be healthy (this may take up to 2 minutes)..."
    
    while [ $attempt -le $max_attempts ]; do
        # Check if container is running first
        if ! docker ps | grep -q s-space-app-prod; then
            echo -n "x"  # Container not running
            sleep 2
            ((attempt++))
            continue
        fi
        
        # Check health endpoint
        if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
            print_success "Application is healthy!"
            return 0
        fi
        
        echo -n "."
        sleep 2
        ((attempt++))
    done
    
    print_error "Health check failed after $max_attempts attempts (2 minutes)"
    print_warning "Application may still be starting. Check logs with: docker-compose -f docker-compose-prod.yml logs -f app"
    return 1
}

# Function to show deployment info
show_deployment_info() {
    echo ""
    echo "================================================"
    print_success "Deployment completed successfully!"
    echo "================================================"
    echo ""
    echo " Application URLs:"
    echo "   - Frontend: https://s-pace.com.vn"
    echo "   - Backend API: https://api.s-pace.com.vn/api"
    echo "   - Health: https://api.s-pace.com.vn/actuator/health"
    echo "   - API Docs: https://api.s-pace.com.vn/swagger-ui/index.html"
    echo ""
    echo " Container Status:"
    docker-compose -f docker-compose-prod.yml ps
    echo ""
    echo " Useful Commands:"
    echo "   - View logs: docker-compose -f docker-compose-prod.yml logs -f app"
    echo "   - Stop services: docker-compose -f docker-compose-prod.yml down"
    echo "   - Restart app: docker-compose -f docker-compose-prod.yml restart app"
    echo ""
    echo " Frontend-Backend Connection Test:"
    echo "   - Test CORS: curl -H \"Origin: https://s-pace.com.vn\" -X OPTIONS https://api.s-pace.com.vn/api/users"
    echo "   - Test API: curl https://api.s-pace.com.vn/api/users"
    echo "   - Test Health: curl https://api.s-pace.com.vn/actuator/health"
    echo "   - Test Swagger: curl https://api.s-pace.com.vn/swagger-ui/index.html"
    echo ""
}

# Main deployment logic
main() {
    # Check if we're in the right directory
    check_project_directory
    
    # Check if this is first time deployment or update
    UPDATE_MODE=${1:-"first"}
    
    if [ "$UPDATE_MODE" = "update" ]; then
        print_status "Starting S-PACE Update Deployment..."
        
        # Check Git status
        check_git_status
        
        # Pull latest code from production branch
        print_status "Pulling latest code from production branch (product)..."
        git fetch origin
        git checkout product
        git pull origin product
        
        # Show what's being deployed
        print_status "Deploying commit: $(git log -1 --oneline)"
        
        # Stop current services
        print_status "Stopping current services..."
        docker-compose -f docker-compose-prod.yml down
        
        # Remove old images
        print_status "Cleaning up old images..."
        docker image prune -f
        docker rmi $(docker images "s-space-be_app" -q) 2>/dev/null || true
        
    else
        print_status "Starting S-PACE First Time Deployment..."
        
        # Update system
        print_status "Updating system packages..."
        sudo apt update && sudo apt upgrade -y
        
        # Install Docker
        print_status "Installing Docker..."
        if ! command_exists docker; then
            curl -fsSL https://get.docker.com -o get-docker.sh
            sudo sh get-docker.sh
            sudo usermod -aG docker $USER
            rm get-docker.sh
            print_success "Docker installed successfully!"
        else
            print_status "Docker is already installed"
        fi
        
        # Install Docker Compose
        print_status "Installing Docker Compose..."
        if ! command_exists docker-compose; then
            sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
            sudo chmod +x /usr/local/bin/docker-compose
            print_success "Docker Compose installed successfully!"
        else
            print_status "Docker Compose is already installed"
        fi
        
        # Create necessary directories
        print_status "Creating directories..."
        mkdir -p uploads/cvs uploads/certificates uploads/avatars logs ssl
        
        # Set permissions
        print_status "Setting permissions..."
        chmod 755 uploads uploads/cvs uploads/certificates uploads/avatars logs
        
        # Check SSL certificate
        check_ssl_certificate
        
        # Copy environment file
        print_status "Setting up environment..."
        if [ ! -f .env ]; then
            if [ -f .env.prod ]; then
                cp .env.prod .env
                print_warning "Please edit .env file with your production values!"
            elif [ -f env.prod.template ]; then
                cp env.prod.template .env
                print_warning "Please edit .env file with your production values!"
                print_warning "Template copied from env.prod.template"
            else
                print_warning "No environment template found! Please create .env file manually."
            fi
        fi
    fi
    
    # Build and start services
    print_status "Building and starting services..."
    docker-compose -f docker-compose-prod.yml build --no-cache
    docker-compose -f docker-compose-prod.yml up -d
    
    # Wait for services to be ready
    if wait_for_health; then
        # Test API endpoints after deployment
        print_status "Testing API endpoints..."
        
        # Test health endpoint
        if curl -f https://api.s-pace.com.vn/actuator/health >/dev/null 2>&1; then
            print_success "Health endpoint is working!"
        else
            print_warning "Health endpoint test failed - check nginx configuration"
        fi
        
        # Test Swagger UI
        if curl -f https://api.s-pace.com.vn/swagger-ui/index.html >/dev/null 2>&1; then
            print_success "Swagger UI is accessible!"
        else
            print_warning "Swagger UI test failed - check OpenAPI configuration"
        fi
        
        show_deployment_info
    else
        print_warning "Health check timed out, but checking if application is actually running..."
        
        # Check if application is responding (even if health check failed)
        sleep 5
        if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
            print_success "Application is actually healthy! Health check may have been too aggressive."
            show_deployment_info
        else
            print_error "Deployment failed - application is not responding"
            echo ""
            print_status "Container status:"
            docker-compose -f docker-compose-prod.yml ps
            echo ""
            print_status "Recent application logs (last 50 lines):"
            docker-compose -f docker-compose-prod.yml logs --tail=50 app
            echo ""
            print_status "To view full logs, run: docker-compose -f docker-compose-prod.yml logs -f app"
            exit 1
        fi
    fi
}

# Trap to handle script interruption
cleanup() {
    print_warning "Script interrupted. Cleaning up..."
    docker-compose -f docker-compose-prod.yml down
    exit 1
}

trap cleanup SIGINT SIGTERM

# Show usage if help is requested
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    echo "S-PACE VPS Deployment Script"
    echo ""
    echo "This script deploys S-PACE backend with API subdomain support:"
    echo "  - Frontend: https://s-pace.com.vn"
    echo "  - Backend API: https://api.s-pace.com.vn"
    echo ""
    echo "Usage:"
    echo "  ./deploy-vps.sh          # First time deployment"
    echo "  ./deploy-vps.sh update   # Update existing deployment"
    echo "  ./deploy-vps.sh --help   # Show this help"
    echo ""
    echo "Prerequisites:"
    echo "  - SSL certificate for s-pace.com.vn, www.s-pace.com.vn, api.s-pace.com.vn"
    echo "  - DNS A record for api.s-pace.com.vn pointing to server IP"
    echo "  - Docker and Docker Compose installed"
    echo ""
    exit 0
fi

# Run main function
main "$@"
