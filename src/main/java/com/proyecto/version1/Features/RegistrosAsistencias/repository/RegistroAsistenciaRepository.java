package com.proyecto.version1.Features.RegistrosAsistencias.repository;

import com.proyecto.version1.Features.RegistrosAsistencias.RegistroAsistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RegistroAsistenciaRepository extends JpaRepository<RegistroAsistencia, UUID> {

    java.util.Optional<RegistroAsistencia> findByEmpresa_IdAndEmpleado_IdAndFecha(UUID empresaId, UUID empleadoId, LocalDate fecha);

    java.util.Optional<RegistroAsistencia> findByIdAndEmpresa_IdAndEmpleado_Id(UUID id, UUID empresaId, UUID empleadoId);

    List<RegistroAsistencia> findByEmpresa_IdAndFechaBetween(UUID empresaId, LocalDate fechaInicio, LocalDate fechaFin);

    List<RegistroAsistencia> findByEmpresa_IdAndEmpleado_IdAndFechaBetween(UUID empresaId, UUID empleadoId, LocalDate fechaInicio, LocalDate fechaFin);

    long countByEmpresa_IdAndFecha(UUID empresaId, LocalDate fecha);

    long countByEmpresa_IdAndFechaAndModalidadAplicada(UUID empresaId, LocalDate fecha, String modalidadAplicada);

    long countByEmpresa_IdAndFechaAndEstadoEntradaIn(UUID empresaId, LocalDate fecha, java.util.Collection<String> estados);

    long countByEmpresa_IdAndFechaAndHoraSalidaIsNullAndModalidadAplicada(UUID empresaId, LocalDate fecha, String modalidadAplicada);

    java.util.List<RegistroAsistencia> findByEmpresa_IdAndEmpleado_IdAndHoraSalidaIsNullOrderByFechaDesc(UUID empresaId, UUID empleadoId);
}


