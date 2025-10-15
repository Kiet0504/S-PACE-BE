-- V3: Add Rating System
-- Add rating columns to users table
ALTER TABLE "user"
ADD COLUMN average_rating DECIMAL(3,2) DEFAULT 0.00,
ADD COLUMN total_ratings INTEGER DEFAULT 0;

-- Create rating table
CREATE TABLE rating (
    rating_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    collaborator_id UUID NOT NULL,
    rated_by UUID NOT NULL,
    rating_score DECIMAL(3,2) NOT NULL CHECK (rating_score >= 1.0 AND rating_score <= 5.0),
    rating_comment TEXT,
    rating_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    punctuality_score DECIMAL(3,2) DEFAULT 0.00 CHECK (punctuality_score >= 0.0 AND punctuality_score <= 5.0),
    quality_score DECIMAL(3,2) DEFAULT 0.00 CHECK (quality_score >= 0.0 AND quality_score <= 5.0),
    attitude_score DECIMAL(3,2) DEFAULT 0.00 CHECK (attitude_score >= 0.0 AND attitude_score <= 5.0),
    teamwork_score DECIMAL(3,2) DEFAULT 0.00 CHECK (teamwork_score >= 0.0 AND teamwork_score <= 5.0),

    -- Constraints
    CONSTRAINT fk_rating_event FOREIGN KEY (event_id) REFERENCES event(event_id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_collaborator FOREIGN KEY (collaborator_id) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_rated_by FOREIGN KEY (rated_by) REFERENCES "user"(user_id) ON DELETE CASCADE,
    CONSTRAINT unique_rating_per_event_collaborator UNIQUE (event_id, collaborator_id, rated_by)
);

-- Indexes for performance
CREATE INDEX idx_rating_event_id ON rating(event_id);
CREATE INDEX idx_rating_collaborator_id ON rating(collaborator_id);
CREATE INDEX idx_rating_rated_by ON rating(rated_by);
CREATE INDEX idx_rating_date ON rating(rating_date);
CREATE INDEX idx_users_average_rating ON "user"(average_rating);

-- Function to update user rating statistics
CREATE OR REPLACE FUNCTION update_user_rating_stats(user_uuid UUID)
RETURNS VOID AS $$
BEGIN
    UPDATE "user"
    SET
        average_rating = (
            SELECT COALESCE(AVG(rating_score), 0.00)
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

-- Trigger after insert
CREATE OR REPLACE FUNCTION trigger_update_rating_stats_insert()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM update_user_rating_stats(NEW.collaborator_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_rating_insert_stats
    AFTER INSERT ON rating
    FOR EACH ROW
    EXECUTE FUNCTION trigger_update_rating_stats_insert();

-- Trigger after update
CREATE OR REPLACE FUNCTION trigger_update_rating_stats_update()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM update_user_rating_stats(NEW.collaborator_id);
    IF OLD.collaborator_id != NEW.collaborator_id THEN
        PERFORM update_user_rating_stats(OLD.collaborator_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_rating_update_stats
    AFTER UPDATE ON rating
    FOR EACH ROW
    EXECUTE FUNCTION trigger_update_rating_stats_update();

-- Trigger after delete
CREATE OR REPLACE FUNCTION trigger_update_rating_stats_delete()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM update_user_rating_stats(OLD.collaborator_id);
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_rating_delete_stats
    AFTER DELETE ON rating
    FOR EACH ROW
    EXECUTE FUNCTION trigger_update_rating_stats_delete();

