package com.medpro.medpro.service;

import com.medpro.medpro.enums.MotivoCancelamento;
import com.medpro.medpro.model.dto.*;
import com.medpro.medpro.model.entity.*;
import com.medpro.medpro.repository.ConsultaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ConsultaServiceTests {
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 23, 8, 0); // segunda
    private static final LocalDateTime DATA = AGORA.plusDays(2);
    private ConsultaRepository repository;
    private ConsultaService service;
    private Paciente paciente;
    private Medico medico;

    @BeforeEach
    void configurar() {
        repository = mock(ConsultaRepository.class);
        service = new ConsultaService(repository) {
            @Override LocalDateTime agora() { return AGORA; }
        };
        paciente = mock(Paciente.class);
        medico = mock(Medico.class);
        when(paciente.getId()).thenReturn(1L);
        when(paciente.getAtivo()).thenReturn(true);
        when(medico.getId()).thenReturn(2L);
        when(medico.getAtivo()).thenReturn(true);
        when(repository.bloquearPaciente(1L)).thenReturn(Optional.of(paciente));
        when(repository.bloquearMedico(2L)).thenReturn(Optional.of(medico));
        when(repository.save(any(Consulta.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test void agendaComMedicoEDuracaoDeUmaHora() {
        var resposta = service.agendar(new DadosAgendamentoConsulta(1L, 2L, DATA));
        assertEquals(1L, resposta.idPaciente());
        assertEquals(2L, resposta.idMedico());
        assertEquals(DATA.plusHours(1), resposta.fim());
        assertNull(resposta.motivoCancelamento());
        verify(repository).save(any(Consulta.class));
    }

    @Test void rejeitaDomingo() {
        assertThrows(IllegalArgumentException.class, () -> ConsultaService.validarHorario(AGORA.plusDays(6), AGORA));
    }

    @Test void aceitaSabadoEHorariosLimite() {
        var sabado = AGORA.plusDays(5);
        assertDoesNotThrow(() -> ConsultaService.validarHorario(sabado.withHour(7), AGORA));
        assertDoesNotThrow(() -> ConsultaService.validarHorario(sabado.withHour(18), AGORA));
    }

    @Test void rejeitaForaDoHorarioOuConsultaQueTerminaDepoisDas19() {
        assertThrows(IllegalArgumentException.class, () -> ConsultaService.validarHorario(DATA.withHour(6), AGORA));
        assertThrows(IllegalArgumentException.class, () -> ConsultaService.validarHorario(DATA.withHour(18).plusMinutes(1), AGORA));
        assertThrows(IllegalArgumentException.class, () -> ConsultaService.validarHorario(DATA.withHour(19), AGORA));
    }

    @Test void validaAntecedenciaMinimaDe30Minutos() {
        assertThrows(IllegalArgumentException.class, () -> ConsultaService.validarHorario(AGORA.plusMinutes(29), AGORA));
        assertDoesNotThrow(() -> ConsultaService.validarHorario(AGORA.plusMinutes(30), AGORA));
    }

    @Test void rejeitaPacienteInativo() {
        when(paciente.getAtivo()).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.agendar(new DadosAgendamentoConsulta(1L, 2L, DATA)));
        verify(repository, never()).save(any(Consulta.class));
    }

    @Test void rejeitaMedicoInativo() {
        when(medico.getAtivo()).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> service.agendar(new DadosAgendamentoConsulta(1L, 2L, DATA)));
    }

    @Test void rejeitaPacienteComConsultaNoMesmoDia() {
        var inicio = DATA.toLocalDate().atStartOfDay();
        when(repository.existsByPaciente_IdAndMotivoCancelamentoIsNullAndDataGreaterThanEqualAndDataLessThan(
                1L, inicio, inicio.plusDays(1))).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.agendar(new DadosAgendamentoConsulta(1L, 2L, DATA)));
    }

    @Test void rejeitaMedicoComHorarioSobreposto() {
        when(repository.existsByMedico_IdAndMotivoCancelamentoIsNullAndDataGreaterThanAndDataLessThan(
                2L, DATA.minusHours(1), DATA.plusHours(1))).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.agendar(new DadosAgendamentoConsulta(1L, 2L, DATA)));
    }

    @Test void selecionaAutomaticamenteMedicoDisponivel() {
        var ocupado = mock(Medico.class);
        when(ocupado.getId()).thenReturn(3L);
        when(ocupado.getAtivo()).thenReturn(true);
        when(repository.bloquearMedicosAtivos()).thenReturn(List.of(ocupado, medico));
        when(repository.existsByMedico_IdAndMotivoCancelamentoIsNullAndDataGreaterThanAndDataLessThan(
                3L, DATA.minusHours(1), DATA.plusHours(1))).thenReturn(true);
        assertEquals(2L, service.agendar(new DadosAgendamentoConsulta(1L, null, DATA)).idMedico());
    }

    @Test void rejeitaQuandoNaoHaMedicoDisponivel() {
        when(repository.bloquearMedicosAtivos()).thenReturn(List.of(medico));
        when(repository.existsByMedico_IdAndMotivoCancelamentoIsNullAndDataGreaterThanAndDataLessThan(
                2L, DATA.minusHours(1), DATA.plusHours(1))).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.agendar(new DadosAgendamentoConsulta(1L, null, DATA)));
    }

    @Test void rejeitaPacienteOuMedicoInexistente() {
        when(repository.bloquearPaciente(99L)).thenReturn(Optional.empty());
        when(repository.bloquearMedico(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.agendar(new DadosAgendamentoConsulta(99L, 2L, DATA)));
        assertThrows(EntityNotFoundException.class, () -> service.agendar(new DadosAgendamentoConsulta(1L, 99L, DATA)));
    }

    @Test void cancelaComMotivoE24HorasDeAntecedencia() {
        for (var motivo : MotivoCancelamento.values()) {
            var consulta = new Consulta(paciente, medico, AGORA.plusHours(24));
            when(repository.bloquearConsulta(1L)).thenReturn(Optional.of(consulta));
            service.cancelar(new DadosCancelamentoConsulta(1L, motivo));
            assertEquals(motivo, consulta.getMotivoCancelamento());
        }
    }

    @Test void rejeitaCancelamentoComMenosDe24Horas() {
        var consulta = new Consulta(paciente, medico, AGORA.plusHours(24).minusMinutes(1));
        when(repository.bloquearConsulta(1L)).thenReturn(Optional.of(consulta));
        assertThrows(IllegalArgumentException.class, () -> service.cancelar(new DadosCancelamentoConsulta(1L, MotivoCancelamento.OUTROS)));
        assertNull(consulta.getMotivoCancelamento());
    }

    @Test void rejeitaCancelamentoSemMotivo() {
        assertThrows(IllegalArgumentException.class, () -> service.cancelar(new DadosCancelamentoConsulta(1L, null)));
    }

    @Test void rejeitaConsultaJaCancelada() {
        var consulta = new Consulta(paciente, medico, DATA);
        consulta.cancelar(MotivoCancelamento.OUTROS);
        when(repository.bloquearConsulta(1L)).thenReturn(Optional.of(consulta));
        assertThrows(IllegalArgumentException.class, () -> service.cancelar(new DadosCancelamentoConsulta(1L, MotivoCancelamento.MEDICO_CANCELOU)));
        assertEquals(MotivoCancelamento.OUTROS, consulta.getMotivoCancelamento());
    }

    @Test void rejeitaCancelamentoDeConsultaInexistente() {
        when(repository.bloquearConsulta(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.cancelar(new DadosCancelamentoConsulta(99L, MotivoCancelamento.OUTROS)));
    }
}
