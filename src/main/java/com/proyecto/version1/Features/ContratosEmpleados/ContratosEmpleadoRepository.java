package com.proyecto.version1.Features.ContratosEmpleados;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContratosEmpleadoRepository extends JpaRepository<ContratosEmpleado, UUID> {

    List<ContratosEmpleado> findByEmpleado_IdAndActivoTrue(UUID empleadoId);

    Optional<ContratosEmpleado> findTopByEmpleado_IdAndActivoTrueAndFechaIngresoLessThanEqualAndFechaRetiroIsNullOrderByFechaIngresoDesc(
            UUID empleadoId,
            LocalDate fechaReferencia
    );

    Optional<ContratosEmpleado> findTopByEmpleado_IdAndActivoTrueAndFechaIngresoLessThanEqualAndFechaRetiroGreaterThanEqualOrderByFechaIngresoDesc(
            UUID empleadoId,
            LocalDate fechaReferencia1,
            LocalDate fechaReferencia2
    );

    Optional<ContratosEmpleado> findTopByEmpleado_IdAndActivoTrueAndFechaIngresoLessThanEqualOrderByFechaIngresoDesc(
            UUID empleadoId,
            LocalDate fechaReferencia
    );
}


