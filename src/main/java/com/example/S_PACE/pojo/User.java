package com.example.S_PACE.pojo;

import com.example.S_PACE.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "\"user\"")
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "role_id", nullable = false)
    Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", referencedColumnName = "team_id")
    Team team;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id")
    Company company;

    @Column(name = "full_name", nullable = false, length = 100)
    String fullName;

    @Column(nullable = false, unique = true, length = 150)
    String email;

    @Column(name = "password_hash", nullable = false)
    String passwordHash;

    String avatar;

    @Column(length = 15)
    String phone;

    @Column(length = 255)
    String address;

    @Column(name = "gender", length = 20)
    String gender;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    UserStatus status = UserStatus.PENDING;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
