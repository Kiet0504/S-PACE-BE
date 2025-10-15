package com.example.S_PACE.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "certificates")
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Certificates {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "certificates_id")
    UUID certificatesId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", referencedColumnName = "event_id", nullable = false)
    Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    User user;

    @Column(name = "certificate_data", columnDefinition = "BYTEA")
    byte[] certificateData;

    @Column(length = 500)
    String certificateFilePath;

    @Column(unique = true, nullable = false, length = 50)
    String certificateCode;

    @Column(nullable = false)
    LocalDate issuedDate;

    @Column(length = 255)
    String issuedBy;

    @jakarta.persistence.Transient
    String certificatePresignedUrl;
}
