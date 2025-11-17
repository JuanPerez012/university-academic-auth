package com.authservice.config;

import com.authservice.enums.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtConfigTest {

    @Test
    void generateAndValidateToken_shouldReturnValidPayload() {
        JwtConfig jwtConfig = new JwtConfig();

        ReflectionTestUtils.setField(jwtConfig,
                "secret",
                "my-super-secret-key-for-tests-1234567890");

        List<RoleName> roles = List.of(RoleName.ROLE_ADMIN, RoleName.ROLE_STUDENT);

        String token = jwtConfig.generateToken("user@example.com", roles, 10);

        assertNotNull(token);

        JwtConfig.JwtPayload payload = jwtConfig.validate(token);

        assertEquals("user@example.com", payload.subject());
        assertEquals(List.of("ROLE_ADMIN", "ROLE_STUDENT"), payload.roles());
        assertNotNull(payload.expiration());
    }

    @Test
    void validate_shouldThrowWhenTokenIsInvalid() {
        JwtConfig jwtConfig = new JwtConfig();
        ReflectionTestUtils.setField(jwtConfig,
                "secret",
                "another-secret-key-for-tests");

        assertThrows(SecurityException.class,
                () -> jwtConfig.validate("invalid-token-value"));
    }
}
