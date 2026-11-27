package com.medpro.medpro.controller;

import com.medpro.medpro.model.dto.DadosAutenticacao;
import com.medpro.medpro.model.entity.Usuario;
import com.medpro.medpro.infra.security.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class AutenticacaoController {
    @Autowired private AuthenticationManager manager;
    @Autowired private TokenService tokenService;
    @PostMapping
    public ResponseEntity<DadosTokenJWT> efetuarLogin(@RequestBody @Valid DadosAutenticacao dados) {
        var authentication = manager.authenticate(new UsernamePasswordAuthenticationToken(dados.login(), dados.senha()));
        return ResponseEntity.ok(new DadosTokenJWT(tokenService.gerarToken((Usuario) authentication.getPrincipal())));
    }
}
