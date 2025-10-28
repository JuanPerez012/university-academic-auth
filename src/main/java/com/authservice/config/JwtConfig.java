package com.authservice.config;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret:defaultsecret1234567890}")
    private String secret;

    public String generateToken(String subject, List<String> roles, int minutes) {
        try {
            JWSSigner signer = new MACSigner(secret.getBytes());
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .claim("roles", roles)
                    .expirationTime(new Date(System.currentTimeMillis() + (minutes * 60 * 1000)))
                    .issueTime(new Date())
                    .build();

            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Error al generar el token JWT", e);
        }
    }

    public Map<String, Object> validate(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret.getBytes());
            if (!jwt.verify(verifier)) throw new RuntimeException("Token inválido");
            if (jwt.getJWTClaimsSet().getExpirationTime().before(new Date()))
                throw new RuntimeException("Token expirado");
            return jwt.getJWTClaimsSet().getClaims();
        } catch (Exception e) {
            throw new RuntimeException("Token inválido", e);
        }
    }
}