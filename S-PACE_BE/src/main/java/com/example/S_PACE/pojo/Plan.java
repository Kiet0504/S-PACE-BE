package com.example.S_PACE.pojo;

import com.example.S_PACE.enums.PlanStatus;
import com.example.S_PACE.enums.PlanType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "plan")
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "plan_id")
    UUID planId;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "company_id", referencedColumnName = "company_id")
    Company company;

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    List<Payment> payments;

    String planName;

    @Enumerated(EnumType.STRING)
    @Column(name = "planType")
    PlanType planType;

    Integer maxEmployee;
    Integer maxEventPerMonth;
    Integer maxCollaborators;
    BigDecimal price;
    int durationMonth;

    @Column(columnDefinition = "TEXT")
    String features;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    PlanStatus status;
}
