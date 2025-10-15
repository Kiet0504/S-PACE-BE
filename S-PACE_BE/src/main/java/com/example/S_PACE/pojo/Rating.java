package com.example.S_PACE.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "rating")
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rating_id")
    UUID ratingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "event_id", nullable = false)
    Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collaborator_id", referencedColumnName = "user_id", nullable = false)
    User collaborator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_by", referencedColumnName = "user_id", nullable = false)
    User ratedBy;

    @Column(name = "rating_score", nullable = false, precision = 3, scale = 2)
    BigDecimal ratingScore;

    @Column(name = "rating_comment", columnDefinition = "TEXT")
    String ratingComment;

    @Column(name = "rating_date", nullable = false)
    LocalDateTime ratingDate;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "punctuality_score", precision = 3, scale = 2)
    BigDecimal punctualityScore;

    @Column(name = "quality_score", precision = 3, scale = 2)
    BigDecimal qualityScore;

    @Column(name = "attitude_score", precision = 3, scale = 2)
    BigDecimal attitudeScore;

    @Column(name = "teamwork_score", precision = 3, scale = 2)
    BigDecimal teamworkScore;

    @PrePersist
    protected void onCreate() {
        if (ratingDate == null) {
            ratingDate = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Rating{" +
                "ratingId=" + ratingId +
                ", event=" + (event != null ? event.getEventId() : null) +
                ", collaborator=" + (collaborator != null ? collaborator.getUserId() : null) +
                ", ratedBy=" + (ratedBy != null ? ratedBy.getUserId() : null) +
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

