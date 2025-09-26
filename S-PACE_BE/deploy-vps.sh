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

# Function to wait for service health
wait_for_health() {
    local max_attempts=30
    local attempt=1
    
    print_status "Waiting for application to be healthy..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
            print_success "Application is healthy!"
            return 0
        fi
        
        echo -n "."
        sleep 2
        ((attempt++))
    done
    
    print_error "Health check failed after $max_attempts attempts"
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
    echo "   - HTTP: http://REDACTED_IP:8080"
    echo "   - Health: http://REDACTED_IP:8080/actuator/health"
    echo "   - API Docs: http://REDACTED_IP:8080/swagger-ui.html"
    echo ""
    echo " Container Status:"
    docker-compose -f docker-compose-prod.yml ps
    echo ""
    echo " Useful Commands:"
    echo "   - View logs: docker-compose -f docker-compose-prod.yml logs -f app"
    echo "   - Stop services: docker-compose -f docker-compose-prod.yml down"
    echo "   - Restart app: docker-compose -f docker-compose-prod.yml restart app"
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
        
        # Pull latest code
        print_status "Pulling latest code from Git..."
        git fetch origin
        git pull origin develop
        
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
        
        # Copy environment file
        print_status "Setting up environment..."
        if [ ! -f .env ]; then
            if [ -f .env.prod ]; then
                cp .env.prod .env
                print_warning "Please edit .env file with your production values!"
            else
                print_warning ".env.prod file not found! Please create .env file manually."
            fi
        fi
    fi
    
    # Build and start services
    print_status "Building and starting services..."
    docker-compose -f docker-compose-prod.yml build --no-cache
    docker-compose -f docker-compose-prod.yml up -d
    
    # Wait for services to be ready
    if wait_for_health; then
        show_deployment_info
    else
        print_error "Deployment failed!"
        echo ""
        print_status "Container status:"
        docker-compose -f docker-compose-prod.yml ps
        echo ""
        print_status "Application logs:"
        docker-compose -f docker-compose-prod.yml logs app
        exit 1
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
    echo "Usage:"
    echo "  ./deploy-vps.sh          # First time deployment"
    echo "  ./deploy-vps.sh update   # Update existing deployment"
    echo "  ./deploy-vps.sh --help   # Show this help"
    echo ""
    exit 0
fi

# Run main function
main "$@"
