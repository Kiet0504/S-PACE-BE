package com.example.S_PACE.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RatingSummaryResponse {
    private UUID collaboratorId;
    private String collaboratorName;
    private BigDecimal averageRating;
    private Integer totalRatings;
    private Map<String, Integer> ratingDistribution;
    private Map<String, BigDecimal> averageScores;
    private List<RatingResponse> recentRatings;

    // Constructors
    public RatingSummaryResponse() {}

    public RatingSummaryResponse(UUID collaboratorId, String collaboratorName, 
                                 BigDecimal averageRating, Integer totalRatings,
                                 Map<String, Integer> ratingDistribution,
                                 Map<String, BigDecimal> averageScores,
                                 List<RatingResponse> recentRatings) {
        this.collaboratorId = collaboratorId;
        this.collaboratorName = collaboratorName;
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
        this.ratingDistribution = ratingDistribution;
        this.averageScores = averageScores;
        this.recentRatings = recentRatings;
    }

    // Getters and Setters
    public UUID getCollaboratorId() {
        return collaboratorId;
    }

    public void setCollaboratorId(UUID collaboratorId) {
        this.collaboratorId = collaboratorId;
    }

    public String getCollaboratorName() {
        return collaboratorName;
    }

    public void setCollaboratorName(String collaboratorName) {
        this.collaboratorName = collaboratorName;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(Integer totalRatings) {
        this.totalRatings = totalRatings;
    }

    public Map<String, Integer> getRatingDistribution() {
        return ratingDistribution;
    }

    public void setRatingDistribution(Map<String, Integer> ratingDistribution) {
        this.ratingDistribution = ratingDistribution;
    }

    public Map<String, BigDecimal> getAverageScores() {
        return averageScores;
    }

    public void setAverageScores(Map<String, BigDecimal> averageScores) {
        this.averageScores = averageScores;
    }

    public List<RatingResponse> getRecentRatings() {
        return recentRatings;
    }

    public void setRecentRatings(List<RatingResponse> recentRatings) {
        this.recentRatings = recentRatings;
    }

    @Override
    public String toString() {
        return "RatingSummaryResponse{" +
                "collaboratorId=" + collaboratorId +
                ", collaboratorName='" + collaboratorName + '\'' +
                ", averageRating=" + averageRating +
                ", totalRatings=" + totalRatings +
                ", ratingDistribution=" + ratingDistribution +
                ", averageScores=" + averageScores +
                ", recentRatings=" + recentRatings +
                '}';
    }
}

