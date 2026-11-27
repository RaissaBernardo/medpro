package com.medpro.medpro.infra.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Profile("prod")
public class UsuarioInicial {

    @Bean
    CommandLineRunner criarUsuario(
            JdbcTemplate jdbc,
            PasswordEncoder encoder,
            @Value("${ADMIN_LOGIN}") String login,
            @Value("${ADMIN_PASSWORD}") String senha) {

        return args -> {
            jdbc.update("""
                INSERT INTO usuarios (login, senha)
                VALUES (?, ?)
                ON CONFLICT (login) DO NOTHING
                """, login, encoder.encode(senha));
        };
    }
}