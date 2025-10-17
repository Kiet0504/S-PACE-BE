-- Create table to track subscription payments
CREATE TABLE subscription_payment (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    plan_id UUID NOT NULL,
    order_code BIGINT NOT NULL UNIQUE,
    amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES subscription_plan(plan_id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_subscription_payment_user_id ON subscription_payment(user_id);
CREATE INDEX idx_subscription_payment_order_code ON subscription_payment(order_code);
CREATE INDEX idx_subscription_payment_status ON subscription_payment(status);

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_subscription_payment_updated_at
    BEFORE UPDATE ON subscription_payment
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();