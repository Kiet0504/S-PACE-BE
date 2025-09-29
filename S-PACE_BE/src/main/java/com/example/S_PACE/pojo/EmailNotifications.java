package com.example.S_PACE.pojo;

import com.example.S_PACE.enums.EmailType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "email_notifications")
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EmailNotifications {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "email_notifications_id")
    UUID emailNotificationsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "event_id")
    Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_registration_id", referencedColumnName = "event_registration_id")
    EventRegistration eventRegistration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    EmailType emailType;

    @Column(nullable = false, length = 255)
    String subject;

    @Column(columnDefinition = "TEXT")
    String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    LocalDateTime sentAt;
}
