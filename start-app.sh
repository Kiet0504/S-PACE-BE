#!/bin/bash

# Startup script cho S-PACE Application với auto database setup

set -e  # Exit on any error

PROFILE=${1:-dev}
DB_NAME="space_db"

if [ "$PROFILE" = "dev" ]; then
    DB_NAME="space_db_dev"
fi

echo "🚀 Starting S-PACE Application..."
echo "📋 Profile: $PROFILE"
echo "🗄️ Database: $DB_NAME"

# Function để check PostgreSQL
check_postgres() {
    echo "🔍 Checking PostgreSQL connection..."

    if ! command -v psql &> /dev/null; then
        echo "❌ PostgreSQL client not found!"
        echo "💡 Install with: sudo apt install postgresql-client"
        exit 1
    fi

    if ! pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
        echo "❌ PostgreSQL server is not running!"
        echo "💡 Start with: sudo service postgresql start"
        echo "💡 Or with Docker: docker run --name postgres -e POSTGRES_PASSWORD=password -p 5432:5432 -d postgres:15"
        exit 1
    fi

    echo "✅ PostgreSQL is running"
}

# Function để tạo database nếu cần
ensure_database() {
    echo "📦 Ensuring database exists..."

    # Kiểm tra database có tồn tại không
    if psql -h localhost -p 5432 -U ${DB_USERNAME:-postgres} -lqt | cut -d \| -f 1 | grep -qw $DB_NAME; then
        echo "✅ Database '$DB_NAME' already exists"
    else
        echo "🔨 Creating database '$DB_NAME'..."
        createdb -h localhost -p 5432 -U ${DB_USERNAME:-postgres} $DB_NAME
        echo "✅ Database '$DB_NAME' created successfully"
    fi
}

# Function để setup môi trường
setup_environment() {
    echo "⚙️ Setting up environment..."

    # Tạo thư mục upload nếu chưa có
    mkdir -p ./uploads/cvs
    mkdir -p ./uploads/certificates

    # Set permissions
    chmod 755 ./uploads
    chmod 755 ./uploads/cvs
    chmod 755 ./uploads/certificates

    echo "✅ Upload directories created"
}

# Function để chạy ứng dụng
start_application() {
    echo "🔄 Starting Spring Boot application..."
    echo "⏳ This may take a few moments..."

    # Export environment variables nếu cần
    export SPRING_PROFILES_ACTIVE=$PROFILE

    # Chạy với Maven
    if [ -f "mvnw" ]; then
        ./mvnw spring-boot:run -Dspring-boot.run.profiles=$PROFILE
    else
        mvn spring-boot:run -Dspring-boot.run.profiles=$PROFILE
    fi
}

# Main execution
main() {
    echo "================================================"
    echo "🎯 S-PACE Event Personnel Management System"
    echo "================================================"

    check_postgres

    if [ "$PROFILE" != "test" ]; then
        ensure_database
    fi

    setup_environment
    start_application
}

# Trap để cleanup khi exit
cleanup() {
    echo ""
    echo "🛑 Application stopped"
    exit 0
}

trap cleanup SIGINT SIGTERM

# Chạy main function
main

echo "🎉 Application started successfully!"
echo "🌐 Access at: http://localhost:8080"
echo "📊 Actuator: http://localhost:8080/actuator"
echo "📚 API Docs: http://localhost:8080/swagger-ui.html"