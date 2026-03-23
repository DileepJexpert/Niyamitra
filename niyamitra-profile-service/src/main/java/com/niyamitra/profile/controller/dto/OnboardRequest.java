package com.niyamitra.profile.controller.dto;

import com.niyamitra.common.enums.IndustryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OnboardRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", message = "Invalid GSTIN format")
        String gstin,
        String udyam,
        @NotBlank String companyName,
        @NotBlank String nicCode,
        @NotBlank String state,
        String district,
        IndustryCategory industryCategory,
        String preferredLanguage,
        @NotBlank String ownerName,
        @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
        String ownerPhone
) {}
