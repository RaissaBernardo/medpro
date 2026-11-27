package com.medpro.medpro.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record DadosAgendamentoConsulta(
    @NotNull @Positive Long idPaciente,
    @Positive Long idMedico,
    @NotNull LocalDateTime data
) {}
