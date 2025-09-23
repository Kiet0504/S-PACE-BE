package com.example.S_PACE.pojo;

import com.example.S_PACE.enums.EventRegistrationStatus;
import com.example.S_PACE.enums.ParticipationStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "eventRegistration")
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "event_registration_id")
    UUID eventRegistrationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "event_id", nullable = false)
    Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime registrationDate;

    @Column(columnDefinition = "TEXT")
    String registrationData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewedBy", referencedColumnName = "user_id")
    User reviewedBy;

    LocalDateTime reviewedAt;

    @Column(columnDefinition = "TEXT")
    String reviewNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EventRegistrationStatus status = EventRegistrationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ParticipationStatus participationStatus = ParticipationStatus.NOT_ATTENDED;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
