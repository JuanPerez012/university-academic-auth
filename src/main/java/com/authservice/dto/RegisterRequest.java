package com.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank
        @Pattern(regexp = "^(STU-|TEA-).*", message = "El código debe iniciar con STU- (estudiante) o TEA- (profesor)")
        String code
) {}