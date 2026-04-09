package com.niyamitra.kavach.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class UserLookupService {

    private final RestClient profileClient;

    public UserLookupService(@Value("${niyamitra.profile-service.url:http://localhost:8081}") String profileUrl) {
        this.profileClient = RestClient.builder().baseUrl(profileUrl).build();
    }

    public String getPhoneByUserId(UUID userId) {
        if (userId == null) return null;

        try {
            // Direct DB lookup would be better, but for decoupled architecture we call Profile Service
            // In production, this should use a cached user registry or shared read model
            log.debug("Looking up phone for user {}", userId);
            return null; // Placeholder — requires Profile Service user-by-id endpoint
        } catch (Exception e) {
            log.error("Failed to look up phone for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public String getOwnerPhoneByTenantId(UUID tenantId) {
        if (tenantId == null) return null;

        try {
            String response = profileClient.get()
                    .uri("/api/v1/tenants/{tenantId}/users", tenantId)
                    .retrieve()
                    .body(String.class);

            // Parse response to find OWNER role user phone
            // In production, use proper DTO deserialization
            log.debug("Looking up owner phone for tenant {}", tenantId);
            return null; // Placeholder — needs JSON parsing of user list
        } catch (Exception e) {
            log.error("Failed to look up owner for tenant {}: {}", tenantId, e.getMessage());
            return null;
        }
    }
}
