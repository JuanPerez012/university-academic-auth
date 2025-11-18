package com.authservice.controller;

import com.authservice.dto.PasswordResetConfirmRequest;
import com.authservice.dto.PasswordResetRequest;
import com.authservice.dto.PasswordResetResponse;
import com.authservice.service.PasswordResetService;
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
class PasswordResetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PasswordResetService passwordResetService;

    @InjectMocks
    private PasswordResetController passwordResetController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(passwordResetController)
                .build();
    }

    @Test
    void requestReset_shouldReturnMessage() throws Exception {
        PasswordResetResponse response =
                new PasswordResetResponse("Si existe una cuenta asociada, se enviará un correo con el código de recuperación.");

        when(passwordResetService.requestPasswordReset(any(PasswordResetRequest.class)))
                .thenReturn(response);

        PasswordResetRequest request = new PasswordResetRequest("user@example.com");

        mockMvc.perform(post("/api/v1/auth/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(response.message())));
    }

    @Test
    void confirmReset_shouldReturnSuccessMessage() throws Exception {
        PasswordResetResponse response =
                new PasswordResetResponse("Contraseña actualizada correctamente.");

        when(passwordResetService.confirmPasswordReset(any(PasswordResetConfirmRequest.class)))
                .thenReturn(response);

        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                "ABC123",
                "NewPass123",
                "NewPass123"
        );

        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(response.message())));
    }
}
