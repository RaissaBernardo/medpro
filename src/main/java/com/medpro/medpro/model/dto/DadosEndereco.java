package com.medpro.medpro.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DadosEndereco(
    @JsonAlias("logradouro") @NotBlank String rua,
    String numero,
    @NotBlank String bairro,
    @NotBlank @Pattern(regexp = "\\d{5}-?\\d{3}") String cep,
    @NotBlank String cidade,
    @JsonAlias("uf") @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String estado,
    String complemento
) {
    
}
