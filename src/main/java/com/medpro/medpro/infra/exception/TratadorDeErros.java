package com.medpro.medpro.infra.exception;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class TratadorDeErros {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> tratarErro404() { return ResponseEntity.notFound().build(); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosErroValidacao>> tratarErro400(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(e.getFieldErrors().stream().map(DadosErroValidacao::new).toList());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<DadosErro> tratarDadosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new DadosErro(e.getMessage()));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DadosErro> tratarJsonInvalido() {
        return ResponseEntity.badRequest().body(new DadosErro("JSON inválido"));
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DadosErro> tratarErro401() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new DadosErro("Login ou senha inválidos"));
    }
    private record DadosErroValidacao(String campo, String mensagem) {
        DadosErroValidacao(FieldError erro) { this(erro.getField(), erro.getDefaultMessage()); }
    }
    private record DadosErro(String mensagem) {}
}
