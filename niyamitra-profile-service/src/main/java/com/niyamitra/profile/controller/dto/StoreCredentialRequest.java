package com.niyamitra.profile.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreCredentialRequest(
        @NotBlank String portalName,
        @NotBlank String username,
        @NotBlank String password
) {}
