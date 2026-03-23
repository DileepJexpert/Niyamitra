package com.niyamitra.profile.model;

import com.niyamitra.common.enums.IndustryCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "niyamitra_tenants", schema = "niyamitra_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NiyamitraTenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 15, unique = true)
    private String gstin;

    @Column(length = 20)
    private String udyam;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "nic_code", length = 10)
    private String nicCode;

    @Column(length = 50)
    private String state;

    @Column(length = 100)
    private String district;

    @Enumerated(EnumType.STRING)
    @Column(name = "industry_category")
    private IndustryCategory industryCategory;

    @Column(name = "preferred_language", length = 5)
    @Builder.Default
    private String preferredLanguage = "hi";

    @CreationTimestamp
    @Column(name = "onboarded_at")
    private Instant onboardedAt;
}
