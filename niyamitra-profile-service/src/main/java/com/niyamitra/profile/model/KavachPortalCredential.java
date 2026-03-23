package com.niyamitra.profile.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "kavach_portal_creds", schema = "niyamitra_profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class KavachPortalCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "portal_name", length = 100, nullable = false)
    private String portalName;

    @Column(name = "encrypted_username", columnDefinition = "TEXT", nullable = false)
    private String encryptedUsername;

    @Column(name = "encrypted_password", columnDefinition = "TEXT", nullable = false)
    private String encryptedPassword;
}
