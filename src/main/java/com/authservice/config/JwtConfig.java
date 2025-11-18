package com.authservice.config;

import com.authservice.enums.RoleName;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JwtConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(String subject, List<RoleName> roles, int minutes) {
        try {
            var signer = new MACSigner(secret.getBytes(StandardCharsets.UTF_8));

            List<String> roleNames = roles.stream()
                    .map(RoleName::name)
                    .toList();

            var claims = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .claim("roles", roleNames)
                    .expirationTime(new Date(System.currentTimeMillis() + (minutes * 60 * 1000L)))
                    .issueTime(new Date())
                    .build();

            var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(signer);

            return jwt.serialize();

        } catch (JOSEException e) {
            throw new SecurityException("Error generando token JWT: No se pudo generar el token JWT", e);
        }
    }

    public JwtPayload validate(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            var verifier = new MACVerifier(secret.getBytes(StandardCharsets.UTF_8));

            if (!jwt.verify(verifier)) {
                logger.warn("Token inválido: firma incorrecta");
                throw new SecurityException("Token inválido");
            }

            var claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime().before(new Date())) {
                logger.warn("Token expirado para usuario {}", claims.getSubject());
                throw new SecurityException("Token expirado");
            }

            return new JwtPayload(
                    claims.getSubject(),
                    (List<String>) claims.getClaim("roles"),
                    claims.getExpirationTime()
            );

        } catch (Exception e) {
            throw new SecurityException("Error al validar token JWT: Token inválido o corrupto", e);
        }
    }

    public record JwtPayload(String subject, List<String> roles, Date expiration) {}
}