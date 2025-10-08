-- Create subscription_plan table
CREATE TABLE subscription_plan (
    plan_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    price DECIMAL(15,2) NOT NULL,
    max_recruitment_limit INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create user_subscription table
CREATE TABLE user_subscription (
    user_subscription_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    plan_id UUID NOT NULL,
    purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plan(plan_id) ON DELETE CASCADE
);

-- Insert default subscription plans
INSERT INTO subscription_plan (name, price, max_recruitment_limit) VALUES
('Basic', 199000.00, 50),
('Pro', 299000.00, 100);

-- Create indexes for better performance
CREATE INDEX idx_user_subscription_user_id ON user_subscription(user_id);
CREATE INDEX idx_user_subscription_purchased_at ON user_subscription(purchased_at);