-- Update BASIC plan price from 5000 to 199000
UPDATE subscription_plan
SET price = 199000.00
WHERE name = 'Basic';

