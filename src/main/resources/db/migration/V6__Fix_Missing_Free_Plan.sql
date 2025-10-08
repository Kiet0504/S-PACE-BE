-- Fix missing Free plan - ensure it exists in the database
-- This is a corrective migration for the V4 migration that may have failed silently
INSERT INTO subscription_plan (name, price, max_recruitment_limit)
SELECT 'Free', 0.00, 30
WHERE NOT EXISTS (
    SELECT 1 FROM subscription_plan WHERE name = 'Free'
);