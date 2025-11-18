package com.authservice.repository;

import com.authservice.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByCode(String code);
    void deleteByExpiresAtBefore(LocalDateTime time);
}