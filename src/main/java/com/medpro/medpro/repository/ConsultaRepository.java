package com.medpro.medpro.repository;

import com.medpro.medpro.model.entity.Consulta;
import com.medpro.medpro.model.entity.Medico;
import com.medpro.medpro.model.entity.Paciente;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Paciente p where p.id = :id")
    Optional<Paciente> bloquearPaciente(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medico m where m.id = :id")
    Optional<Medico> bloquearMedico(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medico m where m.ativo = true order by m.id")
    List<Medico> bloquearMedicosAtivos();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Consulta c where c.id = :id")
    Optional<Consulta> bloquearConsulta(@Param("id") Long id);

    boolean existsByPaciente_IdAndMotivoCancelamentoIsNullAndDataGreaterThanEqualAndDataLessThan(
            Long idPaciente, LocalDateTime inicio, LocalDateTime fim);

    // Duas consultas de uma hora se sobrepoem quando seus inicios distam menos de uma hora.
    boolean existsByMedico_IdAndMotivoCancelamentoIsNullAndDataGreaterThanAndDataLessThan(
            Long idMedico, LocalDateTime limiteInferior, LocalDateTime limiteSuperior);
}
