package com.authservice.service;

import com.authservice.config.JwtConfig;
import com.authservice.dto.LoginRequest;
import com.authservice.dto.RegisterRequest;
import com.authservice.dto.TokenResponse;
import com.authservice.entity.User;
import com.authservice.entity.UserRole;
import com.authservice.enums.RoleName;
import com.authservice.repository.UserRepository;
import com.authservice.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private JwtConfig jwtConfig;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateStudentUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest(
                "student@example.com",
                "password123",
                "STU-0001"
        );

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(jwtConfig.generateToken(eq(request.email()), anyList(), anyInt()))
                .thenReturn("fake-jwt-token");

        TokenResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.token());
        assertEquals("Bearer", response.type());
        assertEquals(1800L, response.expiresIn());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("student@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPasswordHash());

        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(roleCaptor.capture());
        UserRole savedRole = roleCaptor.getValue();
        assertEquals(RoleName.ROLE_STUDENT, savedRole.getRoleName());
        assertSame(savedUser, savedRole.getUser());

        verify(jwtConfig).generateToken(eq(request.email()), anyList(), eq(30));
    }

    @Test
    void register_shouldCreateTeacherUserWhenCodeStartsWithTEA() {
        RegisterRequest request = new RegisterRequest(
                "teacher@example.com",
                "password123",
                "TEA-0001"
        );

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(jwtConfig.generateToken(eq(request.email()), anyList(), anyInt()))
                .thenReturn("fake-jwt-token");

        authService.register(request);

        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(roleCaptor.capture());
        assertEquals(RoleName.ROLE_TEACHER, roleCaptor.getValue().getRoleName());
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "existing@example.com",
                "password123",
                "STU-0001"
        );

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verify(userRoleRepository, never()).save(any(UserRole.class));
        verify(jwtConfig, never()).generateToken(anyString(), anyList(), anyInt());
    }

    @Test
    void login_shouldReturnTokenWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash("encoded-password");

        UserRole role = new UserRole();
        role.setRoleName(RoleName.ROLE_STUDENT);
        role.setUser(user);

        user.getRoles().add(role);

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password"))
                .thenReturn(true);
        when(jwtConfig.generateToken(eq("user@example.com"), anyList(), anyInt()))
                .thenReturn("login-jwt-token");

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("login-jwt-token", response.token());
        assertEquals("Bearer", response.type());
        assertEquals(1800L, response.expiresIn());

        verify(userRepository).findByEmail("user@example.com");
        verify(passwordEncoder).matches("password123", "encoded-password");
        verify(jwtConfig).generateToken(eq("user@example.com"), anyList(), eq(30));
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        LoginRequest request = new LoginRequest("unknown@example.com", "password123");

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(request));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtConfig, never()).generateToken(anyString(), anyList(), anyInt());
    }

    @Test
    void login_shouldThrowWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash("encoded-password");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password"))
                .thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));

        verify(jwtConfig, never()).generateToken(anyString(), anyList(), anyInt());
    }
}
