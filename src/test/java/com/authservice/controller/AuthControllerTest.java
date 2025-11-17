package com.authservice.controller;

import com.authservice.dto.LoginRequest;
import com.authservice.dto.RegisterRequest;
import com.authservice.dto.TokenResponse;
import com.authservice.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .build();
    }

    @Test
    void register_shouldReturnTokenResponse() throws Exception {
        TokenResponse tokenResponse =
                new TokenResponse("fake-jwt", "Bearer", 1800);

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(tokenResponse);

        RegisterRequest request = new RegisterRequest(
                "user@example.com",
                "password123",
                "STU-0001"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("fake-jwt")))
                .andExpect(jsonPath("$.type", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(1800)));
    }

    @Test
    void login_shouldReturnTokenResponse() throws Exception {
        TokenResponse tokenResponse =
                new TokenResponse("login-jwt", "Bearer", 1800);

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(tokenResponse);

        LoginRequest request = new LoginRequest(
                "user@example.com",
                "password123"
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("login-jwt")))
                .andExpect(jsonPath("$.type", is("Bearer")))
                .andExpect(jsonPath("$.expiresIn", is(1800)));
    }
}
