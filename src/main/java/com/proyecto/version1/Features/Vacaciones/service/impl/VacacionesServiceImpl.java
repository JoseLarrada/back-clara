package com.proyecto.version1.Features.Vacaciones.service.impl;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.Features.Vacaciones.SolicitudesVacacione;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesCreateRequest;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesEmpleadoCreateRequest;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesResponse;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesSaldoResponse;
import com.proyecto.version1.Features.Vacaciones.repository.SolicitudesVacacionesRepository;
import com.proyecto.version1.Features.Vacaciones.service.VacacionesService;
import com.proyecto.version1.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VacacionesServiceImpl implements VacacionesService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String APROBADO = "APROBADO";
    private static final String RECHAZADO = "RECHAZADO";

    private final SolicitudesVacacionesRepository solicitudesVacacionesRepository;
    private final EmpleadosRepository empleadosRepository;

    @Override
    public VacacionesResponse crearSolicitud(VacacionesCreateRequest request) {
        UUID tenantId = requireTenant();
        Empleado empleado = obtenerEmpleadoDelTenant(request.empleadoId(), tenantId);
        validarFechas(request.fechaInicio(), request.fechaFin());

        int saldoActual = saldoActual(empleado);
        int diasSolicitados = calcularDias(request.fechaInicio(), request.fechaFin());

        SolicitudesVacacione entity = SolicitudesVacacione.builder()
                .empleado(empleado)
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .estadoSolicitud(PENDIENTE)
                .build();

        SolicitudesVacacione saved = solicitudesVacacionesRepository.save(entity);
        return toResponse(saved, diasSolicitados, saldoActual, saldoActual);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VacacionesResponse> listarPendientes(Pageable pageable) {
        UUID tenantId = requireTenant();
        Page<SolicitudesVacacione> page = solicitudesVacacionesRepository
                .findByEmpleado_EmpresaIdAndEstadoSolicitud(tenantId, PENDIENTE, pageable);

        return new PageResponse<>(
                page.map(solicitud -> toResponse(solicitud, calcularDias(solicitud.getFechaInicio(), solicitud.getFechaFin()),
                        saldoActual(solicitud.getEmpleado()), saldoActual(solicitud.getEmpleado()))).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public VacacionesResponse aprobarSolicitud(UUID solicitudId) {
        UUID tenantId = requireTenant();
        SolicitudesVacacione solicitud = solicitudesVacacionesRepository
                .findByIdAndEmpleado_EmpresaId(solicitudId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de vacaciones no encontrada con ID: " + solicitudId));

        validarEstadoPendiente(solicitud.getEstadoSolicitud());
        validarFechas(solicitud.getFechaInicio(), solicitud.getFechaFin());

        Empleado empleado = solicitud.getEmpleado();
        int saldoAntes = saldoActual(empleado);
        int diasSolicitados = calcularDias(solicitud.getFechaInicio(), solicitud.getFechaFin());

        if (saldoAntes < diasSolicitados) {
            throw new BadRequestException("El empleado no tiene saldo de vacaciones suficiente para aprobar la solicitud");
        }

        empleado.setSaldoVacaciones(saldoAntes - diasSolicitados);
        empleadosRepository.save(empleado);

        solicitud.setEstadoSolicitud(APROBADO);
        SolicitudesVacacione updated = solicitudesVacacionesRepository.save(solicitud);
        return toResponse(updated, diasSolicitados, saldoAntes, saldoAntes - diasSolicitados);
    }

    @Override
    public VacacionesResponse rechazarSolicitud(UUID solicitudId) {
        UUID tenantId = requireTenant();
        SolicitudesVacacione solicitud = solicitudesVacacionesRepository
                .findByIdAndEmpleado_EmpresaId(solicitudId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud de vacaciones no encontrada con ID: " + solicitudId));

        validarEstadoPendiente(solicitud.getEstadoSolicitud());

        Empleado empleado = solicitud.getEmpleado();
        int saldoActual = saldoActual(empleado);
        int diasSolicitados = calcularDias(solicitud.getFechaInicio(), solicitud.getFechaFin());

        solicitud.setEstadoSolicitud(RECHAZADO);
        SolicitudesVacacione updated = solicitudesVacacionesRepository.save(solicitud);
        return toResponse(updated, diasSolicitados, saldoActual, saldoActual);
    }

    @Override
    @Transactional(readOnly = true)
    public VacacionesSaldoResponse consultarSaldo(UUID empleadoId) {
        UUID tenantId = requireTenant();
        Empleado empleado = obtenerEmpleadoDelTenant(empleadoId, tenantId);
        return new VacacionesSaldoResponse(empleado.getId(), empleado.getNombreCompleto(), saldoActual(empleado));
    }

    @Override
    @Transactional(readOnly = true)
    public VacacionesSaldoResponse consultarMiSaldo() {
        UUID tenantId = requireTenant();
        Empleado empleado = requireCurrentEmployee(tenantId);
        return new VacacionesSaldoResponse(empleado.getId(), empleado.getNombreCompleto(), saldoActual(empleado));
    }

    @Override
    public VacacionesResponse crearMiSolicitud(VacacionesEmpleadoCreateRequest request) {
        UUID tenantId = requireTenant();
        Empleado empleado = requireCurrentEmployee(tenantId);

        VacacionesCreateRequest adminLikeRequest = new VacacionesCreateRequest(
                empleado.getId(),
                request.fechaInicio(),
                request.fechaFin()
        );

        return crearSolicitud(adminLikeRequest);
    }

    private Empleado obtenerEmpleadoDelTenant(UUID empleadoId, UUID tenantId) {
        return empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa autenticada."));
    }

    private UUID requireTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no identificado en token JWT.");
        }
        return tenantId;
    }

    private Empleado requireCurrentEmployee(UUID tenantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }

        return empleadosRepository.findByEmailIgnoreCase(authentication.getName())
                .filter(empleado -> tenantId.equals(empleado.getEmpresaId()))
                .orElseThrow(() -> new ResourceNotFoundException("Empleado autenticado no encontrado para la empresa actual."));
    }

    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new BadRequestException("Las fechas de la solicitud son requeridas");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    private void validarEstadoPendiente(String estadoSolicitud) {
        if (!PENDIENTE.equalsIgnoreCase(estadoSolicitud)) {
            throw new BadRequestException("La solicitud ya fue procesada y no puede modificarse nuevamente");
        }
    }

    private int saldoActual(Empleado empleado) {
        return Optional.ofNullable(empleado.getSaldoVacaciones()).orElse(0);
    }

    private int calcularDias(LocalDate fechaInicio, LocalDate fechaFin) {
        validarFechas(fechaInicio, fechaFin);
        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        if (dias > Integer.MAX_VALUE) {
            throw new BadRequestException("El rango de fechas es demasiado grande");
        }
        return (int) dias;
    }

    private VacacionesResponse toResponse(SolicitudesVacacione solicitud, int diasSolicitados, int saldoAntes, int saldoDespues) {
        Empleado empleado = solicitud.getEmpleado();
        return new VacacionesResponse(
                solicitud.getId(),
                empleado.getId(),
                empleado.getNombreCompleto(),
                solicitud.getFechaInicio(),
                solicitud.getFechaFin(),
                diasSolicitados,
                solicitud.getEstadoSolicitud(),
                saldoAntes,
                saldoDespues,
                solicitud.getCreadoEn()
        );
    }
}


