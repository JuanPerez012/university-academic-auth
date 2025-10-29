package com.authservice.controller;

import com.authservice.dto.UpsertUserRequest;
import com.authservice.dto.LoginRequest;
import com.authservice.dto.RegisterRequest;
import com.authservice.dto.TokenResponse;
import com.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import static com.authservice.utils.ApiPaths.AUTHS;

@RestController
@RequestMapping(AUTHS)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService auth;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(auth.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(auth.login(req));
    }

    @PostMapping("/upsert")
    public ResponseEntity<TokenResponse> upsert(@Valid @RequestBody UpsertUserRequest req) {
        return ResponseEntity.ok(auth.upsert(req));
    }
}