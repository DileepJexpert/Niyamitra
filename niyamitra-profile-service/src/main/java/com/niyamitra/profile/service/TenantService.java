package com.niyamitra.profile.service;

import com.niyamitra.common.config.KafkaTopics;
import com.niyamitra.common.event.TenantOnboardedEvent;
import com.niyamitra.profile.controller.dto.*;
import com.niyamitra.profile.model.NiyamitraTenant;
import com.niyamitra.profile.model.NiyamitraUser;
import com.niyamitra.profile.repository.TenantRepository;
import com.niyamitra.profile.repository.UserRepository;
import com.niyamitra.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public NiyamitraTenant onboardTenant(OnboardRequest request) {
        // Check for duplicate GSTIN
        tenantRepository.findByGstin(request.gstin()).ifPresent(t -> {
            throw new IllegalArgumentException("Tenant with GSTIN " + request.gstin() + " already exists");
        });

        NiyamitraTenant tenant = NiyamitraTenant.builder()
                .gstin(request.gstin())
                .udyam(request.udyam())
                .companyName(request.companyName())
                .nicCode(request.nicCode())
                .state(request.state())
                .district(request.district())
                .industryCategory(request.industryCategory())
                .preferredLanguage(request.preferredLanguage() != null ? request.preferredLanguage() : "hi")
                .build();

        tenant = tenantRepository.save(tenant);

        // Create owner user
        NiyamitraUser owner = NiyamitraUser.builder()
                .tenantId(tenant.getId())
                .name(request.ownerName())
                .phone(request.ownerPhone())
                .role(UserRole.OWNER)
                .build();
        userRepository.save(owner);

        // Publish tenant onboarded event
        UUID correlationId = UUID.randomUUID();
        TenantOnboardedEvent event = new TenantOnboardedEvent(
                UUID.randomUUID(),
                tenant.getId(),
                tenant.getNicCode(),
                tenant.getState(),
                tenant.getIndustryCategory(),
                Instant.now(),
                correlationId,
                "NIYAMITRA"
        );
        kafkaTemplate.send(KafkaTopics.TENANT_ONBOARDED, tenant.getId().toString(), event);

        log.info("Tenant onboarded: {} ({}), correlationId={}", tenant.getCompanyName(), tenant.getGstin(), correlationId);
        return tenant;
    }

    public NiyamitraTenant getTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }

    @Transactional
    public NiyamitraTenant updateTenant(UUID tenantId, UpdateTenantRequest request) {
        NiyamitraTenant tenant = getTenant(tenantId);
        if (request.companyName() != null) tenant.setCompanyName(request.companyName());
        if (request.district() != null) tenant.setDistrict(request.district());
        if (request.preferredLanguage() != null) tenant.setPreferredLanguage(request.preferredLanguage());
        if (request.industryCategory() != null) tenant.setIndustryCategory(request.industryCategory());
        return tenantRepository.save(tenant);
    }

    @Transactional
    public NiyamitraUser addUser(UUID tenantId, AddUserRequest request) {
        getTenant(tenantId); // validate tenant exists
        NiyamitraUser user = NiyamitraUser.builder()
                .tenantId(tenantId)
                .name(request.name())
                .phone(request.phone())
                .role(request.role())
                .build();
        return userRepository.save(user);
    }

    public List<NiyamitraUser> listUsers(UUID tenantId) {
        return userRepository.findByTenantId(tenantId);
    }
}
