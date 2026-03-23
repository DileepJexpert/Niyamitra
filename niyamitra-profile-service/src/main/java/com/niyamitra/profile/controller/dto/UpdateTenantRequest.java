package com.niyamitra.profile.controller.dto;

import com.niyamitra.common.enums.IndustryCategory;

public record UpdateTenantRequest(
        String companyName,
        String district,
        String preferredLanguage,
        IndustryCategory industryCategory
) {}
