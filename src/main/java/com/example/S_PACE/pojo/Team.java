package com.example.S_PACE.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "team")
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "team_id")
    UUID teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "event_id", nullable = false)
    Event event;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    List<User> members;

    @Column(name = "team_name", nullable = false, length = 100)
    String teamName;

    @Column(nullable = false)
    int quantity;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt = LocalDateTime.now();
}