package com.authservice.service;

import com.authservice.dto.PasswordResetConfirmRequest;
import com.authservice.dto.PasswordResetRequest;
import com.authservice.dto.PasswordResetResponse;
import com.authservice.entity.PasswordResetToken;
import com.authservice.entity.User;
import com.authservice.repository.PasswordResetTokenRepository;
import com.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void requestPasswordReset_shouldGenerateTokenAndSendEmail_whenUserExists() {
        PasswordResetRequest request = new PasswordResetRequest("user@example.com");

        User user = new User();
        user.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        PasswordResetResponse response = passwordResetService.requestPasswordReset(request);

        assertNotNull(response);
        assertEquals("Si existe una cuenta asociada, se enviará un correo con el código de recuperación.",
                response.message());

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertEquals(user, savedToken.getUser());
        assertNotNull(savedToken.getCode());
        assertNotNull(savedToken.getCreatedAt());
        assertNotNull(savedToken.getExpiresAt());
        assertFalse(savedToken.isUsed());

        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void requestPasswordReset_shouldNotFail_whenUserDoesNotExist() {
        PasswordResetRequest request = new PasswordResetRequest("unknown@example.com");

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        PasswordResetResponse response = passwordResetService.requestPasswordReset(request);

        assertNotNull(response);
        assertEquals("Si existe una cuenta asociada, se enviará un correo con el código de recuperación.",
                response.message());

        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(mailSender, never()).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    void confirmPasswordReset_shouldUpdatePasswordAndMarkTokenUsed() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash("old-hash");

        PasswordResetToken token = new PasswordResetToken();
        token.setCode("ABC123");
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        token.setUsed(false);

        when(tokenRepository.findByCode("ABC123"))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPass123"))
                .thenReturn("new-hash");

        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                "ABC123",
                "NewPass123",
                "NewPass123"
        );

        PasswordResetResponse response = passwordResetService.confirmPasswordReset(request);

        assertEquals("Contraseña actualizada correctamente.", response.message());
        assertEquals("new-hash", user.getPasswordHash());
        assertTrue(token.isUsed());
        verify(tokenRepository).save(token);
    }

    @Test
    void confirmPasswordReset_shouldThrowWhenPasswordsDoNotMatch() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                "ABC123",
                "NewPass123",
                "Different123"
        );

        assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.confirmPasswordReset(request));

        verify(tokenRepository, never()).findByCode(anyString());
    }

    @Test
    void confirmPasswordReset_shouldThrowWhenTokenNotFound() {
        when(tokenRepository.findByCode("INVALID"))
                .thenReturn(Optional.empty());

        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                "INVALID",
                "NewPass123",
                "NewPass123"
        );

        assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.confirmPasswordReset(request));
    }

    @Test
    void confirmPasswordReset_shouldThrowWhenTokenExpiredOrUsed() {
        User user = new User();
        user.setEmail("user@example.com");

        PasswordResetToken expiredToken = new PasswordResetToken();
        expiredToken.setCode("EXPIRED");
        expiredToken.setUser(user);
        expiredToken.setCreatedAt(LocalDateTime.now().minusHours(2));
        expiredToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        expiredToken.setUsed(false);

        when(tokenRepository.findByCode("EXPIRED"))
                .thenReturn(Optional.of(expiredToken));

        PasswordResetConfirmRequest requestExpired = new PasswordResetConfirmRequest(
                "EXPIRED",
                "NewPass123",
                "NewPass123"
        );

        assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.confirmPasswordReset(requestExpired));

        PasswordResetToken usedToken = new PasswordResetToken();
        usedToken.setCode("USED");
        usedToken.setUser(user);
        usedToken.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        usedToken.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        usedToken.setUsed(true);

        when(tokenRepository.findByCode("USED"))
                .thenReturn(Optional.of(usedToken));

        PasswordResetConfirmRequest requestUsed = new PasswordResetConfirmRequest(
                "USED",
                "NewPass123",
                "NewPass123"
        );

        assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.confirmPasswordReset(requestUsed));
    }
}
