package com.authservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtConfig);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_shouldSkipWhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtConfig);
    }

    @Test
    void doFilter_shouldSkipWhenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Token abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtConfig);
    }

    @Test
    void doFilter_shouldSetAuthenticationWhenTokenIsValidAndNoAuthenticationPresent()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        JwtConfig.JwtPayload payload = new JwtConfig.JwtPayload(
                "user@example.com",
                List.of("ADMIN", "STUDENT"),
                Date.from(Instant.now().plusSeconds(600))
        );

        when(jwtConfig.validate("valid-token")).thenReturn(payload);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication, "La autenticación no debería ser nula");
        assertEquals("user@example.com", authentication.getName(), "El username debe coincidir con el subject");
        assertTrue(
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")),
                "Debe contener la autoridad ROLE_ADMIN"
        );
        assertTrue(
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")),
                "Debe contener la autoridad ROLE_STUDENT"
        );

        verify(jwtConfig).validate("valid-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldNotOverrideExistingAuthentication() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer another-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        JwtConfig.JwtPayload payload = new JwtConfig.JwtPayload(
                "other@example.com",
                List.of("TEACHER"),
                Date.from(Instant.now().plusSeconds(600))
        );
        when(jwtConfig.validate("another-token")).thenReturn(payload);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertSame(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        verify(jwtConfig).validate("another-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldContinueChainWhenJwtValidationFails() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtConfig.validate("invalid-token"))
                .thenThrow(new RuntimeException("Token inválido"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(jwtConfig).validate("invalid-token");
    }
}
