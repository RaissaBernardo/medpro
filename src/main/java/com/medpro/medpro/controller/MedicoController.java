package com.medpro.medpro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.persistence.EntityNotFoundException;
import com.medpro.medpro.model.dto.DadosDetalhamentoMedico;

import com.medpro.medpro.model.dto.DadosAtualizacaoMedico;
import com.medpro.medpro.model.dto.DadosCadastroMedico;
import com.medpro.medpro.model.dto.DadosListagemMedico;
import com.medpro.medpro.model.entity.Medico;
import com.medpro.medpro.repository.MedicoRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("medicos")
public class MedicoController {

    @Autowired
    private MedicoRepository medicoRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoMedico> cadastrar(@RequestBody @Valid DadosCadastroMedico dados, UriComponentsBuilder uriBuilder) {
        var medico = medicoRepository.save(new Medico(dados));
        var uri = uriBuilder.path("/medicos/{id}").buildAndExpand(medico.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoMedico(medico));
    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemMedico>> listar(@PageableDefault(size = 10, sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(medicoRepository.findAllByAtivoTrue(paginacao).map(DadosListagemMedico::new));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoMedico> atualizar(@RequestBody @Valid DadosAtualizacaoMedico dados) {
        var medico = medicoRepository.findById(dados.id()).orElseThrow(EntityNotFoundException::new);
        medico.atualizarInformacoes(dados);
        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        var medico = medicoRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        medico.excluir();
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<DadosDetalhamentoMedico> detalhar(@PathVariable Long id) {
        var medico = medicoRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }
}
