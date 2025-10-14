package com.example.S_PACE.pojo;

import com.example.S_PACE.enums.EventRegistrationStatus;
import com.example.S_PACE.enums.ParticipationStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "eventRegistration")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "event_registration_id")
    UUID eventRegistrationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", referencedColumnName = "event_id", nullable = false)
    Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    User user;

    @CreationTimestamp
    @Column(name = "registration_date", nullable = false, updatable = false)
    LocalDateTime registrationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", referencedColumnName = "user_id")
    User reviewedBy;

    @Column(name = "reviewed_at")
    LocalDateTime reviewedAt;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    String reviewNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    EventRegistrationStatus status = EventRegistrationStatus.PENDING;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    // ========== THÔNG TIN CỘNG TÁC VIÊN ==========

    @Column(name = "full_name")
    String fullName;

    @Column(name = "gender")
    String gender;

    @Column(name = "profession")
    String profession;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "address", columnDefinition = "TEXT")
    String address;

    @Column(name = "reason_for_participation", columnDefinition = "TEXT")
    String reasonForParticipation;

    @Column(name = "file_path")
    String filePath; // Gộp CV và Portfolio thành một file

    @Column(name = "birth_year")
    Integer birthYear;
}
