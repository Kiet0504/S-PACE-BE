-- Add the Free plan that was missing from the initial subscription plans
-- Use INSERT ... ON CONFLICT to avoid duplicate key errors
INSERT INTO subscription_plan (name, price, max_recruitment_limit) VALUES
('Free', 0.00, 30)
ON CONFLICT (name) DO NOTHING;