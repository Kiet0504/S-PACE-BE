-- V11: Update Rating Score Calculation
-- Make rating_score a computed column based on the average of 4 detailed scores

-- First, update existing records to calculate rating_score from the 4 detailed scores
UPDATE rating 
SET rating_score = ROUND(
    (COALESCE(punctuality_score, 0) + 
     COALESCE(quality_score, 0) + 
     COALESCE(attitude_score, 0) + 
     COALESCE(teamwork_score, 0)) / 4.0, 2
)
WHERE rating_score IS NOT NULL;

-- Add a check constraint to ensure rating_score is calculated correctly
-- Only add if it doesn't exist to avoid conflicts
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_rating_score_calculation' 
        AND table_name = 'rating'
    ) THEN
        ALTER TABLE rating 
        ADD CONSTRAINT chk_rating_score_calculation 
        CHECK (
            rating_score = ROUND(
                (COALESCE(punctuality_score, 0) + 
                 COALESCE(quality_score, 0) + 
                 COALESCE(attitude_score, 0) + 
                 COALESCE(teamwork_score, 0)) / 4.0, 2
            )
        );
    END IF;
END $$;

-- Update the function to calculate average rating from the 4 detailed scores
CREATE OR REPLACE FUNCTION update_user_rating_stats(user_uuid UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE "user"
    SET
        average_rating = (
            SELECT COALESCE(
                ROUND(
                    (AVG(COALESCE(punctuality_score, 0)) + 
                     AVG(COALESCE(quality_score, 0)) + 
                     AVG(COALESCE(attitude_score, 0)) + 
                     AVG(COALESCE(teamwork_score, 0))) / 4.0, 2
                ), 0.00
            )
            FROM rating
            WHERE collaborator_id = user_uuid
        ),
        total_ratings = (
            SELECT COUNT(*)
            FROM rating
            WHERE collaborator_id = user_uuid
        )
    WHERE user_id = user_uuid;
END;
$$ LANGUAGE plpgsql;

-- Create a trigger function to automatically calculate rating_score when detailed scores are updated
CREATE OR REPLACE FUNCTION calculate_rating_score()
RETURNS TRIGGER AS $$
BEGIN
    -- Calculate rating_score as average of 4 detailed scores
    NEW.rating_score := ROUND(
        (COALESCE(NEW.punctuality_score, 0) + 
         COALESCE(NEW.quality_score, 0) + 
         COALESCE(NEW.attitude_score, 0) + 
         COALESCE(NEW.teamwork_score, 0)) / 4.0, 2
    );
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for INSERT (only if it doesn't exist)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.triggers 
        WHERE trigger_name = 'tr_calculate_rating_score_insert'
    ) THEN
        CREATE TRIGGER tr_calculate_rating_score_insert
            BEFORE INSERT ON rating
            FOR EACH ROW
            EXECUTE FUNCTION calculate_rating_score();
    END IF;
END $$;

-- Create trigger for UPDATE (only if it doesn't exist)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.triggers 
        WHERE trigger_name = 'tr_calculate_rating_score_update'
    ) THEN
        CREATE TRIGGER tr_calculate_rating_score_update
            BEFORE UPDATE ON rating
            FOR EACH ROW
            EXECUTE FUNCTION calculate_rating_score();
    END IF;
END $$;

