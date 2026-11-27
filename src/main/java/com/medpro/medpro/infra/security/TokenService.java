package com.medpro.medpro.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.medpro.medpro.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario) {
        return JWT.create().withIssuer("API MedPro")
                .withSubject(usuario.getLogin())
                .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                .sign(Algorithm.HMAC256(secret));
    }
    public String getSubject(String tokenJWT) {
        return JWT.require(Algorithm.HMAC256(secret)).withIssuer("API MedPro")
                .build().verify(tokenJWT).getSubject();
    }
}
