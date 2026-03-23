package com.niyamitra.profile.controller.dto;

import com.niyamitra.common.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AddUserRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
        String phone,
        @NotNull UserRole role
) {}
