package com.authservice.service;

import com.authservice.entity.User;
import com.authservice.entity.UserRole;
import com.authservice.enums.RoleName;
import com.authservice.repository.UserRepository;
import com.authservice.repository.UserRoleRepository;
import com.authservice.config.JwtConfig;
import com.authservice.dto.LoginRequest;
import com.authservice.dto.RegisterRequest;
import com.authservice.dto.TokenResponse;
import com.authservice.dto.UpsertUserRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final JwtConfig jwt;
    private final PasswordEncoder encoder;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            logger.warn("Intento de registro con email existente: {}", request.email());
            throw new RuntimeException("El usuario ya existe");
        }

        RoleName assignedRole = determineRoleFromCode(request.code());
        logger.info("Registrando usuario {} con rol {}", request.email(), assignedRole);

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(encoder.encode(request.password()));
        userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setRoleName(assignedRole);
        userRole.setUser(user);
        roleRepository.save(userRole);
        user.getRoles().add(userRole);

        String token = jwt.generateToken(user.getEmail(), List.of(userRole.getRoleName()), 30);

        logger.info("Usuario {} registrado exitosamente con rol {}", user.getEmail(), assignedRole);
        return new TokenResponse(token, "Bearer", 1800);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        List<RoleName> roles = user.getRoles().stream()
                .map(UserRole::getRoleName)
                .collect(Collectors.toList());

        String token = jwt.generateToken(user.getEmail(), roles, 30);
        return new TokenResponse(token, "Bearer", 1800);
    }

    @Transactional
    public TokenResponse upsert(UpsertUserRequest r) {
        var existing = userRepository.findByEmail(r.email());

        if (existing.isEmpty()) {
            var u = new User();
            u.setEmail(r.email());
            String raw = (r.password() != null && !r.password().isBlank())
                    ? r.password()
                    : generateTempPassword();
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

            boolean hasRole = u.getRoles().stream()
                    .anyMatch(rr -> rr.getRoleName() == r.role());

            if (!hasRole) {
                var role = new UserRole();
                role.setUser(u);
                role.setRoleName(r.role());
                roleRepository.save(role);
                u.getRoles().add(role);
            }

            List<RoleName> roles = u.getRoles().stream()
                    .map(UserRole::getRoleName)
                    .collect(Collectors.toList());

            String token = jwt.generateToken(u.getEmail(), roles, 30);
            return new TokenResponse(token, "Bearer", 1800);
        }
    }

    private RoleName determineRoleFromCode(String code) {
        if (code == null || code.isBlank()) {
            logger.warn("Código vacío o nulo, asignando rol por defecto: ROLE_STUDENT");
            return RoleName.ROLE_STUDENT;
        }

        String upperCode = code.trim().toUpperCase();

        if (upperCode.startsWith("TEA-")) {
            logger.debug("Código {} identificado como profesor", code);
            return RoleName.ROLE_TEACHER;
        } else if (upperCode.startsWith("STU-")) {
            logger.debug("Código {} identificado como estudiante", code);
            return RoleName.ROLE_STUDENT;
        }

        logger.warn("Código {} no reconocido, asignando rol por defecto: ROLE_STUDENT", code);
        return RoleName.ROLE_STUDENT;
    }

    private String generateTempPassword() {
        var rnd = new SecureRandom();
        byte[] buf = new byte[12];
        rnd.nextBytes(buf);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}