package com.example.S_PACE.repository;

import com.example.S_PACE.pojo.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {
    
    // Check if rating already exists for event-collaborator-rater combination
    @Query("SELECT COUNT(r) > 0 FROM Rating r WHERE r.event.eventId = :eventId AND r.collaborator.userId = :collaboratorId AND r.ratedBy.userId = :ratedBy")
    boolean existsByEventIdAndCollaboratorIdAndRatedBy(@Param("eventId") UUID eventId, @Param("collaboratorId") UUID collaboratorId, @Param("ratedBy") UUID ratedBy);
    
    // Find rating by event-collaborator-rater combination
    @Query("SELECT r FROM Rating r WHERE r.event.eventId = :eventId AND r.collaborator.userId = :collaboratorId AND r.ratedBy.userId = :ratedBy")
    Optional<Rating> findByEventIdAndCollaboratorIdAndRatedBy(@Param("eventId") UUID eventId, @Param("collaboratorId") UUID collaboratorId, @Param("ratedBy") UUID ratedBy);
    
    // Find all ratings for a specific collaborator
    @Query("SELECT r FROM Rating r WHERE r.collaborator.userId = :collaboratorId ORDER BY r.ratingDate DESC")
    Page<Rating> findByCollaboratorIdOrderByRatingDateDesc(@Param("collaboratorId") UUID collaboratorId, Pageable pageable);
    
    // Find all ratings for a specific event
    @Query("SELECT r FROM Rating r WHERE r.event.eventId = :eventId ORDER BY r.ratingDate DESC")
    List<Rating> findByEventIdOrderByRatingDateDesc(@Param("eventId") UUID eventId);
    
    // Find rating for specific event-collaborator pair
    @Query("SELECT r FROM Rating r WHERE r.event.eventId = :eventId AND r.collaborator.userId = :collaboratorId")
    Optional<Rating> findByEventIdAndCollaboratorId(@Param("eventId") UUID eventId, @Param("collaboratorId") UUID collaboratorId);
    
    // Custom query to get ratings with user and event details
    @Query("SELECT r FROM Rating r " +
           "JOIN FETCH r.event e " +
           "JOIN FETCH r.collaborator c " +
           "JOIN FETCH r.ratedBy rb " +
           "WHERE r.collaborator.userId = :collaboratorId " +
           "ORDER BY r.ratingDate DESC")
    Page<Rating> findByCollaboratorIdWithDetails(@Param("collaboratorId") UUID collaboratorId, Pageable pageable);
    
    // Get rating statistics for a collaborator - calculate from 4 detailed scores
    @Query("SELECT AVG((r.punctualityScore + r.qualityScore + r.attitudeScore + r.teamworkScore) / 4.0) FROM Rating r WHERE r.collaborator.userId = :collaboratorId")
    Double getAverageRatingByCollaboratorId(@Param("collaboratorId") UUID collaboratorId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.collaborator.userId = :collaboratorId")
    Long getTotalRatingsByCollaboratorId(@Param("collaboratorId") UUID collaboratorId);
    
    // Get rating distribution - calculate from 4 detailed scores
    @Query("SELECT CAST(ROUND((r.punctualityScore + r.qualityScore + r.attitudeScore + r.teamworkScore) / 4.0) AS int) as score, COUNT(r) as count " +
           "FROM Rating r WHERE r.collaborator.userId = :collaboratorId " +
           "GROUP BY CAST(ROUND((r.punctualityScore + r.qualityScore + r.attitudeScore + r.teamworkScore) / 4.0) AS int) " +
           "ORDER BY score DESC")
    List<Object[]> getRatingDistributionByCollaboratorId(@Param("collaboratorId") UUID collaboratorId);
    
    // Get average scores for different criteria
    @Query("SELECT AVG(r.punctualityScore) FROM Rating r WHERE r.collaborator.userId = :collaboratorId AND r.punctualityScore > 0")
    Double getAveragePunctualityScore(@Param("collaboratorId") UUID collaboratorId);
    
    @Query("SELECT AVG(r.qualityScore) FROM Rating r WHERE r.collaborator.userId = :collaboratorId AND r.qualityScore > 0")
    Double getAverageQualityScore(@Param("collaboratorId") UUID collaboratorId);
    
    @Query("SELECT AVG(r.attitudeScore) FROM Rating r WHERE r.collaborator.userId = :collaboratorId AND r.attitudeScore > 0")
    Double getAverageAttitudeScore(@Param("collaboratorId") UUID collaboratorId);
    
    @Query("SELECT AVG(r.teamworkScore) FROM Rating r WHERE r.collaborator.userId = :collaboratorId AND r.teamworkScore > 0")
    Double getAverageTeamworkScore(@Param("collaboratorId") UUID collaboratorId);
    
    // Get recent ratings for a collaborator (last 5)
    @Query("SELECT r FROM Rating r " +
           "JOIN FETCH r.event e " +
           "JOIN FETCH r.ratedBy rb " +
           "WHERE r.collaborator.userId = :collaboratorId " +
           "ORDER BY r.ratingDate DESC")
    List<Rating> getRecentRatingsByCollaboratorId(@Param("collaboratorId") UUID collaboratorId, Pageable pageable);
}

