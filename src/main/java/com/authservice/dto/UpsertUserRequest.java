package com.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpsertUserRequest(
        @Email @NotBlank String email,
        @NotBlank String role,
        String password
) {}