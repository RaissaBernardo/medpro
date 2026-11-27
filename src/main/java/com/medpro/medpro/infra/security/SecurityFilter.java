package com.medpro.medpro.infra.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.medpro.medpro.repository.UsuarioRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    @Autowired private TokenService tokenService;
    @Autowired private UsuarioRepository repository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "POST".equals(request.getMethod()) && "/login".equals(request.getServletPath());
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        var header = request.getHeader("Authorization");
        if (header != null) {
            try {
                if (!header.startsWith("Bearer ")) throw new JWTVerificationException("Formato inválido");
                var subject = tokenService.getSubject(header.substring(7));
                if (subject == null || subject.isBlank()) throw new JWTVerificationException("Token inválido");
                var usuario = repository.findByLogin(subject);
                if (usuario == null) throw new JWTVerificationException("Usuário inexistente");
                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JWTVerificationException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"mensagem\":\"Token JWT inválido ou expirado!\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
