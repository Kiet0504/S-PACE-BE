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
@Table(name = "user")
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleId", referencedColumnName = "roleId", nullable = false)
    Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teamId", referencedColumnName = "teamId")
    Team team;

    @Column(nullable = false, length = 100)
    String fullName;

    @Column(nullable = false, unique = true, length = 150)
    String email;

    @Column(nullable = false)
    String passwordHash;

    String avatar;

    @Column(length = 15)
    String phone;

    @Column(length = 255)
    String address;

    @Column(nullable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    UserStatus status = UserStatus.PENDING;
}
