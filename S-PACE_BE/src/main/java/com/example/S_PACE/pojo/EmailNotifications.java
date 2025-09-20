package com.example.S_PACE.pojo;

import com.example.S_PACE.enums.EmailType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

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
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID emailNotificationsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId", referencedColumnName = "eventId")
    Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventRegistrationId", referencedColumnName = "eventRegistrationId")
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
