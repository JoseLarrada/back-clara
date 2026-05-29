package com.proyecto.version1.Features.Calendario.repository;

import com.proyecto.version1.Features.Calendario.CalendarioHibrido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarioHibridoRepository extends JpaRepository<CalendarioHibrido, UUID> {
    Optional<CalendarioHibrido> findByEmpleado_IdAndFecha(UUID empleadoId, LocalDate fecha);
    Optional<CalendarioHibrido> findByIdAndEmpleado_EmpresaId(UUID id, UUID empresaId);
    List<CalendarioHibrido> findByEmpleado_EmpresaIdAndFechaBetween(UUID empresaId, LocalDate desde, LocalDate hasta);
    List<CalendarioHibrido> findByEmpleado_IdAndFechaBetween(UUID empleadoId, LocalDate desde, LocalDate hasta);
}

