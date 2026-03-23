package com.niyamitra.profile.repository;

import com.niyamitra.profile.model.KavachPortalCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortalCredentialRepository extends JpaRepository<KavachPortalCredential, UUID> {

    Optional<KavachPortalCredential> findByTenantIdAndPortalName(UUID tenantId, String portalName);
}
