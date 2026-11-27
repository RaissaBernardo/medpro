package com.medpro.medpro.service;

import com.medpro.medpro.model.dto.*;
import com.medpro.medpro.model.entity.Consulta;
import com.medpro.medpro.model.entity.Medico;
import com.medpro.medpro.repository.ConsultaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ConsultaService {
    private static final ZoneId FUSO_CLINICA = ZoneId.of("America/Sao_Paulo");
    private static final LocalTime ABERTURA = LocalTime.of(7, 0);
    private static final LocalTime FECHAMENTO = LocalTime.of(19, 0);
    private final ConsultaRepository repository;

    public ConsultaService(ConsultaRepository repository) {
        this.repository = repository;
    }

    // Bloqueios pessimistas impedem agendamentos simultaneos conflitantes.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DadosDetalhamentoConsulta agendar(DadosAgendamentoConsulta dados) {
        validarHorario(dados.data(), agora());
        var paciente = repository.bloquearPaciente(dados.idPaciente())
                .orElseThrow(EntityNotFoundException::new);
        if (!Boolean.TRUE.equals(paciente.getAtivo())) {
            throw new IllegalArgumentException("Paciente inativo não pode agendar consultas.");
        }
        var inicioDia = dados.data().toLocalDate().atStartOfDay();
        if (repository.existsByPaciente_IdAndMotivoCancelamentoIsNullAndDataGreaterThanEqualAndDataLessThan(
                paciente.getId(), inicioDia, inicioDia.plusDays(1))) {
            throw new IllegalArgumentException("Paciente já possui consulta agendada neste dia.");
        }

        Medico medico;
        if (dados.idMedico() != null) {
            medico = repository.bloquearMedico(dados.idMedico()).orElseThrow(EntityNotFoundException::new);
            if (!Boolean.TRUE.equals(medico.getAtivo())) {
                throw new IllegalArgumentException("Médico inativo não pode receber consultas.");
            }
            if (!disponivel(medico, dados.data())) {
                throw new IllegalArgumentException("Médico já possui consulta nesse horário.");
            }
        } else {
            var disponiveis = repository.bloquearMedicosAtivos().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getAtivo()))
                    .filter(m -> disponivel(m, dados.data())).toList();
            if (disponiveis.isEmpty()) {
                throw new IllegalArgumentException("Nenhum médico disponível nesse horário.");
            }
            medico = disponiveis.get(ThreadLocalRandom.current().nextInt(disponiveis.size()));
        }
        // Revalida a antecedencia depois de adquirir os bloqueios.
        validarHorario(dados.data(), agora());
        return new DadosDetalhamentoConsulta(repository.save(new Consulta(paciente, medico, dados.data())));
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoConsulta detalhar(Long id) {
        return new DadosDetalhamentoConsulta(repository.findById(id).orElseThrow(EntityNotFoundException::new));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void cancelar(DadosCancelamentoConsulta dados) {
        if (dados.motivo() == null) {
            throw new IllegalArgumentException("Motivo do cancelamento é obrigatório.");
        }
        var consulta = repository.bloquearConsulta(dados.idConsulta()).orElseThrow(EntityNotFoundException::new);
        if (consulta.getMotivoCancelamento() != null) {
            throw new IllegalArgumentException("Consulta já cancelada.");
        }
        validarCancelamento(consulta.getData(), agora());
        consulta.cancelar(dados.motivo());
    }

    private boolean disponivel(Medico medico, LocalDateTime data) {
        return !repository.existsByMedico_IdAndMotivoCancelamentoIsNullAndDataGreaterThanAndDataLessThan(
                medico.getId(), data.minusHours(1), data.plusHours(1));
    }

    static void validarHorario(LocalDateTime data, LocalDateTime agora) {
        if (data == null) {
            throw new IllegalArgumentException("Data da consulta é obrigatória.");
        }
        if (data.getDayOfWeek() == DayOfWeek.SUNDAY || data.toLocalTime().isBefore(ABERTURA)
                || data.toLocalTime().isAfter(FECHAMENTO.minusHours(1))) {
            throw new IllegalArgumentException("Consultas devem ocorrer de segunda a sábado, das 07h às 19h, com duração de uma hora.");
        }
        if (data.isBefore(agora.plusMinutes(30))) {
            throw new IllegalArgumentException("Agendamento exige antecedência mínima de 30 minutos.");
        }
    }

    static void validarCancelamento(LocalDateTime data, LocalDateTime agora) {
        if (data.isBefore(agora.plusHours(24))) {
            throw new IllegalArgumentException("Cancelamento exige antecedência mínima de 24 horas.");
        }
    }

    LocalDateTime agora() {
        return LocalDateTime.now(FUSO_CLINICA);
    }
}
