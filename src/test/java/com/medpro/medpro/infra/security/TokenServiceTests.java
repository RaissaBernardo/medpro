package com.medpro.medpro.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.medpro.medpro.model.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTests {
    private static final String SECRET = "chave-exclusiva-para-testes-locais";
    private TokenService service;

    @BeforeEach
    void configurar() {
        service = new TokenService();
        ReflectionTestUtils.setField(service, "secret", SECRET);
    }

    @Test
    void geraEValidaToken() {
        var token = service.gerarToken(new Usuario(1L, "teste@medpro.com", "hash"));
        assertEquals("teste@medpro.com", service.getSubject(token));
        assertEquals("API MedPro", JWT.decode(token).getIssuer());
        assertTrue(JWT.decode(token).getExpiresAt().toInstant().isAfter(Instant.now()));
    }

    @Test
    void rejeitaAssinaturaInvalida() {
        var token = JWT.create().withIssuer("API MedPro").withSubject("teste")
                .withExpiresAt(Instant.now().plusSeconds(60)).sign(Algorithm.HMAC256("outra-chave"));
        assertThrows(JWTVerificationException.class, () -> service.getSubject(token));
    }

    @Test
    void rejeitaTokenExpirado() {
        var token = JWT.create().withIssuer("API MedPro").withSubject("teste")
                .withExpiresAt(Instant.now().minusSeconds(60)).sign(Algorithm.HMAC256(SECRET));
        assertThrows(JWTVerificationException.class, () -> service.getSubject(token));
    }

    @Test
    void senhaDidaticaConfereComMigration() {
        assertTrue(new BCryptPasswordEncoder().matches("123456",
            "$2a$10$7qoiGo8MY204lRyCiRhg2uwaHGK10JU6XDP.UdYGAuxw.dZde8C.6"));
    }
}
