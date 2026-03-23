package com.niyamitra.profile.controller;

import com.niyamitra.profile.controller.dto.*;
import com.niyamitra.profile.model.NiyamitraTenant;
import com.niyamitra.profile.model.NiyamitraUser;
import com.niyamitra.profile.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping("/onboard")
    public ResponseEntity<NiyamitraTenant> onboardTenant(@Valid @RequestBody OnboardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.onboardTenant(request));
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<NiyamitraTenant> getTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(tenantService.getTenant(tenantId));
    }

    @PutMapping("/{tenantId}")
    public ResponseEntity<NiyamitraTenant> updateTenant(@PathVariable UUID tenantId,
                                                         @Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(tenantService.updateTenant(tenantId, request));
    }

    @PostMapping("/{tenantId}/users")
    public ResponseEntity<NiyamitraUser> addUser(@PathVariable UUID tenantId,
                                                  @Valid @RequestBody AddUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.addUser(tenantId, request));
    }

    @GetMapping("/{tenantId}/users")
    public ResponseEntity<List<NiyamitraUser>> listUsers(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(tenantService.listUsers(tenantId));
    }
}
