-- Add password reset fields to user table
ALTER TABLE "user"
ADD COLUMN reset_password_token VARCHAR(255),
ADD COLUMN reset_password_token_expiry BIGINT;

-- Add index for faster token lookup
CREATE INDEX idx_user_reset_password_token ON "user"(reset_password_token);

-- Add comments for documentation
COMMENT ON COLUMN "user".reset_password_token IS 'Token used for password reset, expires after 15 minutes';
COMMENT ON COLUMN "user".reset_password_token_expiry IS 'Timestamp (in milliseconds) when the reset token expires';
