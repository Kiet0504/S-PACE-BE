package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.CreateRatingRequest;
import com.example.S_PACE.dto.request.UpdateRatingRequest;
import com.example.S_PACE.dto.response.RatingListResponse;
import com.example.S_PACE.dto.response.RatingResponse;
import com.example.S_PACE.dto.response.RatingSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RatingService {
    
    /**
     * Create a new rating
     */
    RatingResponse createRating(CreateRatingRequest request, UUID currentUserId);
    
    /**
     * Update an existing rating
     */
    RatingResponse updateRating(UUID ratingId, UpdateRatingRequest request, UUID currentUserId);
    
    /**
     * Delete a rating
     */
    void deleteRating(UUID ratingId, UUID currentUserId);
    
    /**
     * Get rating by ID
     */
    RatingResponse getRatingById(UUID ratingId);
    
    /**
     * Get all ratings for a collaborator with pagination
     */
    RatingListResponse getRatingsByCollaborator(UUID collaboratorId, Pageable pageable);
    
    /**
     * Get all ratings for an event
     */
    RatingListResponse getRatingsByEvent(UUID eventId);
    
    /**
     * Get specific rating for event-collaborator pair
     */
    RatingResponse getRatingByEventAndCollaborator(UUID eventId, UUID collaboratorId);
    
    /**
     * Get rating summary for a collaborator
     */
    RatingSummaryResponse getRatingSummary(UUID collaboratorId);
    
    /**
     * Check if user can rate a collaborator for an event
     */
    boolean canRate(UUID eventId, UUID collaboratorId, UUID currentUserId);
}

