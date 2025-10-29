package com.authservice.service;

import com.authservice.entity.User;
import com.authservice.entity.UserRole;
import com.authservice.repository.UserRepository;
import com.authservice.repository.UserRoleRepository;
import com.authservice.config.JwtConfig;
import com.authservice.dto.LoginRequest;
import com.authservice.dto.RegisterRequest;
import com.authservice.dto.TokenResponse;
import com.authservice.dto.UpsertUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final JwtConfig jwt;
    private final PasswordEncoder encoder;

    public TokenResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent())
            throw new RuntimeException("El usuario ya existe");

        User u = new User();
        u.setEmail(request.email());
        u.setPasswordHash(encoder.encode(request.password()));
        userRepository.save(u);

        UserRole r = new UserRole();
        r.setRoleName(request.role());
        r.setUser(u);
        roleRepository.save(r);
        u.getRoles().add(r);

        String token = jwt.generateToken(u.getEmail(), List.of(r.getRoleName()), 30);
        return new TokenResponse(token, "Bearer", 1800);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!encoder.matches(request.password(), user.getPasswordHash()))
            throw new RuntimeException("Credenciales inválidas");

        List<String> roles = user.getRoles().stream().map(UserRole::getRoleName).toList();
        String token = jwt.generateToken(user.getEmail(), roles, 30);
        return new TokenResponse(token, "Bearer", 1800);
    }

    public TokenResponse upsert(UpsertUserRequest r) {
        var existing = userRepository.findByEmail(r.email());
        if (existing.isEmpty()) {
            var u = new User();
            u.setEmail(r.email());
            String raw = (r.password() != null && !r.password().isBlank()) ? r.password() : generateTempPassword();
            u.setPasswordHash(encoder.encode(raw));
            userRepository.save(u);

            var role = new UserRole();
            role.setUser(u);
            role.setRoleName(r.role());
            roleRepository.save(role);
            u.getRoles().add(role);

            String token = jwt.generateToken(u.getEmail(), List.of(role.getRoleName()), 30);
            return new TokenResponse(token, "Bearer", 1800);
        } else {
            var u = existing.get();
            boolean hasRole = u.getRoles().stream().anyMatch(rr -> rr.getRoleName().equalsIgnoreCase(r.role()));
            if (!hasRole) {
                var role = new UserRole();
                role.setUser(u);
                role.setRoleName(r.role());
                roleRepository.save(role);
                u.getRoles().add(role);
            }
            List<String> roles = u.getRoles().stream().map(UserRole::getRoleName).toList();
            String token = jwt.generateToken(u.getEmail(), roles, 30);
            return new TokenResponse(token, "Bearer", 1800);
        }
    }

    private String generateTempPassword() {
        var rnd = new SecureRandom();
        byte[] buf = new byte[12];
        rnd.nextBytes(buf);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}