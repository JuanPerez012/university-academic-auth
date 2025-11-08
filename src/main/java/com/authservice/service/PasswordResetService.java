package com.authservice.service;

import com.authservice.dto.PasswordResetConfirmRequest;
import com.authservice.dto.PasswordResetRequest;
import com.authservice.dto.PasswordResetResponse;
import com.authservice.entity.PasswordResetToken;
import com.authservice.entity.User;
import com.authservice.repository.PasswordResetTokenRepository;
import com.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Transactional
    public PasswordResetResponse requestPasswordReset(PasswordResetRequest request) {
        userRepository.findByEmail(request.email()).ifPresentOrElse(user -> {
            String code = generateCode();

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setCode(code);
            resetToken.setCreatedAt(LocalDateTime.now());
            resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
            tokenRepository.save(resetToken);

            sendResetEmail(user.getEmail(), code);

            logger.info("Código de recuperación generado para {}: {}", user.getEmail(), code);
        }, () -> logger.info("Solicitud de recuperación recibida para email inexistente: {}", request.email()));

        return new PasswordResetResponse("Si existe una cuenta asociada, se enviará un correo con el código de recuperación.");
    }

    @Transactional
    public PasswordResetResponse confirmPasswordReset(PasswordResetConfirmRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        PasswordResetToken resetToken = tokenRepository.findByCode(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Código inválido o expirado"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Código inválido o expirado");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        logger.info("Contraseña actualizada correctamente para usuario: {}", user.getEmail());
        return new PasswordResetResponse("Contraseña actualizada correctamente.");
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendResetEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Recuperación de contraseña");
        message.setText("Tu código de recuperación es: " + code + "\nEste código expirará en 1 hora.");
        mailSender.send(message);
    }
}