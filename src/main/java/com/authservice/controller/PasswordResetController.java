package com.authservice.controller;

import com.authservice.dto.PasswordResetConfirmRequest;
import com.authservice.dto.PasswordResetRequest;
import com.authservice.dto.PasswordResetResponse;
import com.authservice.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.authservice.utils.ApiPaths.AUTHS;

@RestController
@RequestMapping(AUTHS)
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/password-reset")
    public ResponseEntity<PasswordResetResponse> requestReset(@Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(passwordResetService.requestPasswordReset(request));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<PasswordResetResponse> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        return ResponseEntity.ok(passwordResetService.confirmPasswordReset(request));
    }
}