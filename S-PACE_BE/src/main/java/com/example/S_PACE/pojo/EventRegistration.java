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
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID eventRegistrationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId", referencedColumnName = "eventId", nullable = false)
    Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false)
    User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime registrationDate;

    @Column(columnDefinition = "TEXT")
    String registrationData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewedBy", referencedColumnName = "userId")
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
