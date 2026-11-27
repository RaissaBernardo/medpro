package com.medpro.medpro.model.entity;

import com.medpro.medpro.model.dto.DadosEndereco;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable 
@Getter 
@NoArgsConstructor 
@AllArgsConstructor 
public class Endereco {
    private String rua;
    private String bairro;
    private String cep;
    private String cidade;
    private String estado;
    private String numero;
    private String complemento;
    
    public Endereco(DadosEndereco dados) {
        this.rua = dados.rua();
        this.bairro = dados.bairro();
        this.cep = dados.cep();
        this.cidade = dados.cidade();
        this.estado = dados.estado();
        this.numero = dados.numero();
        this.complemento = dados.complemento();
    }

    public void atualizarInformacoes(DadosEndereco dados) {
        if (dados.rua() != null) {
            if (dados.rua().isBlank())
                throw new IllegalArgumentException("Rua não pode estar em branco.");
            else this.rua = dados.rua();
        }
        if (dados.bairro() != null) {
            if (dados.bairro().isBlank())
                throw new IllegalArgumentException("Bairro não pode estar em branco.");
            else this.bairro = dados.bairro();
        }
        if (dados.cep() != null) {
            if (!dados.cep().matches("\\d{5}-?\\d{3}"))
                throw new IllegalArgumentException("CEP inválido.");
            else this.cep = dados.cep();
        }
        if (dados.cidade() != null) {
            if (dados.cidade().isBlank())
                throw new IllegalArgumentException("Cidade não pode estar em branco.");
            else this.cidade = dados.cidade();
        }
        if (dados.estado() != null) {
            if (!dados.estado().matches("[A-Za-z]{2}"))
                throw new IllegalArgumentException("Estado deve conter duas letras.");
            else this.estado = dados.estado();
        }
        if (dados.numero() != null) this.numero = dados.numero();
        if (dados.complemento() != null) this.complemento = dados.complemento();
    }
}
