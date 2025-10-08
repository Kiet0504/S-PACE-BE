-- init-database.sql
-- Script tạo database và user (chạy với quyền superuser)

-- Tạo database nếu chưa có
SELECT 'CREATE DATABASE space_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'space_db')\gexec

-- Tạo user nếu chưa có (optional)
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'space_user') THEN

      CREATE ROLE space_user LOGIN PASSWORD 'REDACTED_PASSWORD';
   END IF;
END
$do$;

-- Cấp quyền cho user
GRANT ALL PRIVILEGES ON DATABASE space_db TO space_user;

-- Kết nối vào database space_db để cấp quyền schema
\c space_db

-- Cấp quyền trên schema public
GRANT ALL ON SCHEMA public TO space_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO space_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO space_user;