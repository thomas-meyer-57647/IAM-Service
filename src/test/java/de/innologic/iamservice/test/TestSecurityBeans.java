package de.innologic.iamservice.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class TestSecurityBeans {

    @Bean
    JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .claims(c -> {
                    c.putAll(Map.of(
                            "sub", "test-user",
                            "iss", "https://auth.example.com",
                            "jti", "jwt-test",
                            "tenant_id", "COMPANY-100",
                            "subject_type", "USER"
                    ));
                    c.put("aud", List.of("iam-service"));
                })
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
