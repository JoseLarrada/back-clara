package com.proyecto.version1.Features.Vacaciones.repository;

import com.proyecto.version1.Features.Vacaciones.SolicitudesVacacione;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitudesVacacionesRepository extends JpaRepository<SolicitudesVacacione, UUID> {

    Page<SolicitudesVacacione> findByEmpleado_EmpresaIdAndEstadoSolicitud(UUID empresaId, String estadoSolicitud, Pageable pageable);

    List<SolicitudesVacacione> findByEmpleado_EmpresaIdAndEstadoSolicitudOrderByCreadoEnDesc(UUID empresaId, String estadoSolicitud);

    List<SolicitudesVacacione> findByEmpleado_EmpresaIdAndEstadoSolicitudAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            UUID empresaId,
            String estadoSolicitud,
            java.time.LocalDate fechaFin,
            java.time.LocalDate fechaInicio);

    Optional<SolicitudesVacacione> findByIdAndEmpleado_EmpresaId(UUID id, UUID empresaId);

    Optional<SolicitudesVacacione> findByIdAndEmpleado_IdAndEmpleado_EmpresaId(UUID id, UUID empleadoId, UUID empresaId);
}

