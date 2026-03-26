package com.niyamitra.profile.service;

import com.niyamitra.common.exception.ResourceNotFoundException;
import com.niyamitra.profile.controller.dto.StoreCredentialRequest;
import com.niyamitra.profile.model.KavachPortalCredential;
import com.niyamitra.profile.repository.PortalCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortalCredentialService {

    private final PortalCredentialRepository credentialRepository;
    private final EncryptionService encryptionService;
    private final TenantService tenantService;

    @Transactional
    public KavachPortalCredential storeCredential(UUID tenantId, StoreCredentialRequest request) {
        tenantService.getTenant(tenantId); // validate tenant exists

        // Update existing or create new
        KavachPortalCredential credential = credentialRepository
                .findByTenantIdAndPortalName(tenantId, request.portalName())
                .orElse(KavachPortalCredential.builder()
                        .tenantId(tenantId)
                        .portalName(request.portalName())
                        .build());

        credential.setEncryptedUsername(encryptionService.encrypt(request.username()));
        credential.setEncryptedPassword(encryptionService.encrypt(request.password()));

        log.info("Stored portal credentials for tenant {} portal {}", tenantId, request.portalName());
        return credentialRepository.save(credential);
    }

    public KavachPortalCredential getCredential(UUID tenantId, String portalName) {
        return credentialRepository.findByTenantIdAndPortalName(tenantId, portalName)
                .orElseThrow(() -> new ResourceNotFoundException("Portal credential", portalName));
    }

    public String getDecryptedUsername(UUID tenantId, String portalName) {
        KavachPortalCredential cred = getCredential(tenantId, portalName);
        return encryptionService.decrypt(cred.getEncryptedUsername());
    }

    public String getDecryptedPassword(UUID tenantId, String portalName) {
        KavachPortalCredential cred = getCredential(tenantId, portalName);
        return encryptionService.decrypt(cred.getEncryptedPassword());
    }
}
