package com.niyamitra.profile.model;

import com.niyamitra.common.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "niyamitra_users", schema = "niyamitra_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NiyamitraUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private String name;

    @Column(length = 15, nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
}
