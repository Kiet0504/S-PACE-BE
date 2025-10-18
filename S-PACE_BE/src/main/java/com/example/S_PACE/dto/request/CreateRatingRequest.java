package com.example.S_PACE.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public class CreateRatingRequest {
    
    @NotNull(message = "Event ID is required")
    private UUID eventId;
    
    @NotNull(message = "Collaborator ID is required")
    private UUID collaboratorId;
    
    // ratingScore is now calculated automatically from the 4 detailed scores
    
    @Size(max = 1000, message = "Rating comment cannot exceed 1000 characters")
    private String ratingComment;
    
    @DecimalMin(value = "0.0", message = "Punctuality score must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Punctuality score must be at most 5.0")
    private BigDecimal punctualityScore;
    
    @DecimalMin(value = "0.0", message = "Quality score must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Quality score must be at most 5.0")
    private BigDecimal qualityScore;
    
    @DecimalMin(value = "0.0", message = "Attitude score must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Attitude score must be at most 5.0")
    private BigDecimal attitudeScore;
    
    @DecimalMin(value = "0.0", message = "Teamwork score must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Teamwork score must be at most 5.0")
    private BigDecimal teamworkScore;

    // Constructors
    public CreateRatingRequest() {}

    public CreateRatingRequest(UUID eventId, UUID collaboratorId, 
                              String ratingComment, BigDecimal punctualityScore, 
                              BigDecimal qualityScore, BigDecimal attitudeScore, 
                              BigDecimal teamworkScore) {
        this.eventId = eventId;
        this.collaboratorId = collaboratorId;
        this.ratingComment = ratingComment;
        this.punctualityScore = punctualityScore;
        this.qualityScore = qualityScore;
        this.attitudeScore = attitudeScore;
        this.teamworkScore = teamworkScore;
    }

    // Getters and Setters
    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getCollaboratorId() {
        return collaboratorId;
    }

    public void setCollaboratorId(UUID collaboratorId) {
        this.collaboratorId = collaboratorId;
    }

    // ratingScore getter and setter removed - it's now calculated automatically

    public String getRatingComment() {
        return ratingComment;
    }

    public void setRatingComment(String ratingComment) {
        this.ratingComment = ratingComment;
    }

    public BigDecimal getPunctualityScore() {
        return punctualityScore;
    }

    public void setPunctualityScore(BigDecimal punctualityScore) {
        this.punctualityScore = punctualityScore;
    }

    public BigDecimal getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(BigDecimal qualityScore) {
        this.qualityScore = qualityScore;
    }

    public BigDecimal getAttitudeScore() {
        return attitudeScore;
    }

    public void setAttitudeScore(BigDecimal attitudeScore) {
        this.attitudeScore = attitudeScore;
    }

    public BigDecimal getTeamworkScore() {
        return teamworkScore;
    }

    public void setTeamworkScore(BigDecimal teamworkScore) {
        this.teamworkScore = teamworkScore;
    }

    @Override
    public String toString() {
        return "CreateRatingRequest{" +
                "eventId=" + eventId +
                ", collaboratorId=" + collaboratorId +
                ", ratingComment='" + ratingComment + '\'' +
                ", punctualityScore=" + punctualityScore +
                ", qualityScore=" + qualityScore +
                ", attitudeScore=" + attitudeScore +
                ", teamworkScore=" + teamworkScore +
                '}';
    }
}
