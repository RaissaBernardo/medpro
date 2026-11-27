package com.medpro.medpro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.persistence.EntityNotFoundException;
import com.medpro.medpro.model.dto.DadosDetalhamentoPaciente;

import com.medpro.medpro.model.dto.DadosAtualizacaoPaciente;
import com.medpro.medpro.model.dto.DadosCadastroPaciente;
import com.medpro.medpro.model.dto.DadosListagemPaciente;
import com.medpro.medpro.model.entity.Paciente;
import com.medpro.medpro.repository.PacienteRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("pacientes")
public class PacienteController {

    @Autowired
    private PacienteRepository pacienteRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoPaciente> cadastrar(@RequestBody @Valid DadosCadastroPaciente dados, UriComponentsBuilder uriBuilder) {
        var paciente = pacienteRepository.save(new Paciente(dados));
        var uri = uriBuilder.path("/pacientes/{id}").buildAndExpand(paciente.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoPaciente(paciente));
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemPaciente>> listar(@PageableDefault(size = 10, sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(pacienteRepository.findAllByAtivoTrue(paginacao).map(DadosListagemPaciente::new));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoPaciente> atualizar(@RequestBody @Valid DadosAtualizacaoPaciente dados) {
        var paciente = pacienteRepository.findById(dados.id()).orElseThrow(EntityNotFoundException::new);
        paciente.atualizarInformacoes(dados);
        return ResponseEntity.ok(new DadosDetalhamentoPaciente(paciente));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        var paciente = pacienteRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        paciente.excluir();
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoPaciente> detalhar(@PathVariable Long id) {
        var paciente = pacienteRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return ResponseEntity.ok(new DadosDetalhamentoPaciente(paciente));
    }
}
