package com.authservice.dto;

public record TokenResponse(String token, String type, long expiresIn) {}