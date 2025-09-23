package com.example.S_PACE.pojo;

import com.example.S_PACE.enums.CompanyStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "company")
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "company_id")
    UUID companyId;

    @OneToOne(mappedBy = "company", fetch = FetchType.LAZY)
    Plan plan;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<User> users;

    @Column(nullable = false, length = 255)
    String companyName;

    @Column(length = 255)
    String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CompanyStatus status;
}
