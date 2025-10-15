package com.example.S_PACE.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class RatingResponse {
    private UUID ratingId;
    private UUID eventId;
    private String eventTitle;
    private UUID collaboratorId;
    private String collaboratorName;
    private String collaboratorEmail;
    private UUID ratedById;
    private String ratedByName;
    private BigDecimal ratingScore;
    private String ratingComment;
    private LocalDateTime ratingDate;
    private LocalDateTime updatedAt;
    private BigDecimal punctualityScore;
    private BigDecimal qualityScore;
    private BigDecimal attitudeScore;
    private BigDecimal teamworkScore;

    // Constructors
    public RatingResponse() {}

    public RatingResponse(UUID ratingId, UUID eventId, String eventTitle, UUID collaboratorId, 
                          String collaboratorName, String collaboratorEmail, UUID ratedById, 
                          String ratedByName, BigDecimal ratingScore, String ratingComment, 
                          LocalDateTime ratingDate, LocalDateTime updatedAt, 
                          BigDecimal punctualityScore, BigDecimal qualityScore, 
                          BigDecimal attitudeScore, BigDecimal teamworkScore) {
        this.ratingId = ratingId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.collaboratorId = collaboratorId;
        this.collaboratorName = collaboratorName;
        this.collaboratorEmail = collaboratorEmail;
        this.ratedById = ratedById;
        this.ratedByName = ratedByName;
        this.ratingScore = ratingScore;
        this.ratingComment = ratingComment;
        this.ratingDate = ratingDate;
        this.updatedAt = updatedAt;
        this.punctualityScore = punctualityScore;
        this.qualityScore = qualityScore;
        this.attitudeScore = attitudeScore;
        this.teamworkScore = teamworkScore;
    }

    // Getters and Setters
    public UUID getRatingId() {
        return ratingId;
    }

    public void setRatingId(UUID ratingId) {
        this.ratingId = ratingId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

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

    public String getCollaboratorEmail() {
        return collaboratorEmail;
    }

    public void setCollaboratorEmail(String collaboratorEmail) {
        this.collaboratorEmail = collaboratorEmail;
    }

    public UUID getRatedById() {
        return ratedById;
    }

    public void setRatedById(UUID ratedById) {
        this.ratedById = ratedById;
    }

    public String getRatedByName() {
        return ratedByName;
    }

    public void setRatedByName(String ratedByName) {
        this.ratedByName = ratedByName;
    }

    public BigDecimal getRatingScore() {
        return ratingScore;
    }

    public void setRatingScore(BigDecimal ratingScore) {
        this.ratingScore = ratingScore;
    }

    public String getRatingComment() {
        return ratingComment;
    }

    public void setRatingComment(String ratingComment) {
        this.ratingComment = ratingComment;
    }

    public LocalDateTime getRatingDate() {
        return ratingDate;
    }

    public void setRatingDate(LocalDateTime ratingDate) {
        this.ratingDate = ratingDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
        return "RatingResponse{" +
                "ratingId=" + ratingId +
                ", eventId=" + eventId +
                ", eventTitle='" + eventTitle + '\'' +
                ", collaboratorId=" + collaboratorId +
                ", collaboratorName='" + collaboratorName + '\'' +
                ", collaboratorEmail='" + collaboratorEmail + '\'' +
                ", ratedById=" + ratedById +
                ", ratedByName='" + ratedByName + '\'' +
                ", ratingScore=" + ratingScore +
                ", ratingComment='" + ratingComment + '\'' +
                ", ratingDate=" + ratingDate +
                ", updatedAt=" + updatedAt +
                ", punctualityScore=" + punctualityScore +
                ", qualityScore=" + qualityScore +
                ", attitudeScore=" + attitudeScore +
                ", teamworkScore=" + teamworkScore +
                '}';
    }
}

