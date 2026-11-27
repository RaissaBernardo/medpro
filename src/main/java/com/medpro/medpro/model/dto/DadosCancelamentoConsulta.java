package com.medpro.medpro.model.dto;

import com.medpro.medpro.enums.MotivoCancelamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DadosCancelamentoConsulta(
    @NotNull @Positive Long idConsulta,
    @NotNull MotivoCancelamento motivo
) {}
