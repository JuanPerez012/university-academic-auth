package com.authservice.dto;

import com.authservice.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpsertUserRequest(
        @Email @NotBlank String email,
        String password,
        @NotNull RoleName role
) {}