package com.niyamitra.anupalan.model;

import com.niyamitra.common.enums.ComplianceCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "anupalan_rules", schema = "anupalan_rules")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AnupalanRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceCategory category;

    @Column(nullable = false)
    private String authority;

    @Column(name = "applicable_states", columnDefinition = "TEXT[]")
    private String applicableStates;

    @Column(name = "applicable_nic_codes", columnDefinition = "TEXT[]")
    private String applicableNicCodes;

    @Column(name = "industry_categories", columnDefinition = "TEXT[]")
    private String industryCategories;

    @Column(name = "renewal_period_months")
    private Integer renewalPeriodMonths;

    @Column(name = "penalty_description", columnDefinition = "TEXT")
    private String penaltyDescription;

    @Column(name = "required_documents", columnDefinition = "jsonb")
    private String requiredDocuments;

    @Column(name = "portal_url", length = 500)
    private String portalUrl;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_hi", columnDefinition = "TEXT")
    private String descriptionHi;
}
