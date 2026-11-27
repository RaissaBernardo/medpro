package com.medpro.medpro.infra.security;

import com.medpro.medpro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {
    @Autowired private UsuarioRepository repository;
    @Override
    public UserDetails loadUserByUsername(String username) {
        var usuario = repository.findByLogin(username);
        if (usuario == null) throw new UsernameNotFoundException("Usuário não encontrado");
        return usuario;
    }
}
