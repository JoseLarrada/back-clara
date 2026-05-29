package com.proyecto.version1.Features.Backups_Incidencias.service.impl;

import com.proyecto.version1.Features.Backups_Incidencias.BackupIncidenciasJustificacione;
import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionCreateRequest;
import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionResponse;
import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionRevisionRequest;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Backups_Incidencias.repository.BackupIncidenciasJustificacionesRepository;
import com.proyecto.version1.Features.Backups_Incidencias.service.BackupIncidenciasService;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.Features.RegistrosAsistencias.RegistroAsistencia;
import com.proyecto.version1.Features.RegistrosAsistencias.repository.RegistroAsistenciaRepository;
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

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BackupIncidenciasServiceImpl implements BackupIncidenciasService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String APROBADO = "APROBADO";
    private static final String RECHAZADO = "RECHAZADO";
    private static final String FALTA_JUSTIFICADA = "FALTA_JUSTIFICADA";

    private final BackupIncidenciasJustificacionesRepository justificacionesRepository;
    private final RegistroAsistenciaRepository registroAsistenciaRepository;
    private final EmpleadosRepository empleadosRepository;

    @Override
    public JustificacionResponse crearJustificacion(JustificacionCreateRequest request) {
        UUID tenantId = requireTenant();
        RegistroAsistencia registro = obtenerRegistroDelTenant(request.registroAsistenciaId(), tenantId);

        BackupIncidenciasJustificacione entity = BackupIncidenciasJustificacione.builder()
                .registroAsistencia(registro)
                .motivoEmpleado(request.motivoEmpleado())
                .urlComprobanteS3(request.urlComprobanteS3())
                .estadoSolicitud(PENDIENTE)
                .build();

        BackupIncidenciasJustificacione saved = justificacionesRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public JustificacionResponse crearJustificacionEmpleado(JustificacionCreateRequest request) {
        UUID tenantId = requireTenant();
        Empleado empleado = requireCurrentEmployee(tenantId);

        RegistroAsistencia registro = registroAsistenciaRepository
                .findByIdAndEmpresa_IdAndEmpleado_Id(request.registroAsistenciaId(), tenantId, empleado.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Registro de asistencia no encontrado para el empleado autenticado."));

        if (!esEstadoJustificable(registro.getEstadoEntrada())) {
            throw new BadRequestException("Solo se pueden justificar registros en estado RETARDO o FALTA.");
        }

        BackupIncidenciasJustificacione entity = BackupIncidenciasJustificacione.builder()
                .registroAsistencia(registro)
                .motivoEmpleado(request.motivoEmpleado())
                .urlComprobanteS3(request.urlComprobanteS3())
                .estadoSolicitud(PENDIENTE)
                .build();

        BackupIncidenciasJustificacione saved = justificacionesRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<JustificacionResponse> listarPendientes(Pageable pageable) {
        UUID tenantId = requireTenant();
        Page<BackupIncidenciasJustificacione> page = justificacionesRepository
                .findPendientesByEmpresaId(tenantId, PENDIENTE, pageable);

        return new PageResponse<>(
                page.map(this::toResponse).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public JustificacionResponse aprobarJustificacion(UUID justificacionId, JustificacionRevisionRequest request) {
        UUID tenantId = requireTenant();
        BackupIncidenciasJustificacione justificacion = justificacionesRepository
                .findByIdAndEmpresaId(justificacionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Justificación no encontrada con ID: " + justificacionId));

        validarEstadoPendiente(justificacion.getEstadoSolicitud());

        RegistroAsistencia registro = justificacion.getRegistroAsistencia();
        registro.setEstadoEntrada(FALTA_JUSTIFICADA);
        registroAsistenciaRepository.save(registro);

        justificacion.setEstadoSolicitud(APROBADO);
        justificacion.setComentariosAdministrador(comentarios(request));
        justificacion.setProcesadoEn(OffsetDateTime.now());
        BackupIncidenciasJustificacione updated = justificacionesRepository.save(justificacion);
        return toResponse(updated);
    }

    @Override
    public JustificacionResponse rechazarJustificacion(UUID justificacionId, JustificacionRevisionRequest request) {
        UUID tenantId = requireTenant();
        BackupIncidenciasJustificacione justificacion = justificacionesRepository
                .findByIdAndEmpresaId(justificacionId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Justificación no encontrada con ID: " + justificacionId));

        validarEstadoPendiente(justificacion.getEstadoSolicitud());

        justificacion.setEstadoSolicitud(RECHAZADO);
        justificacion.setComentariosAdministrador(comentarios(request));
        justificacion.setProcesadoEn(OffsetDateTime.now());
        BackupIncidenciasJustificacione updated = justificacionesRepository.save(justificacion);
        return toResponse(updated);
    }

    private RegistroAsistencia obtenerRegistroDelTenant(UUID registroId, UUID tenantId) {
        RegistroAsistencia registro = registroAsistenciaRepository.findById(registroId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de asistencia no encontrado con ID: " + registroId));

        if (registro.getEmpresa() == null || !tenantId.equals(registro.getEmpresa().getId())) {
            throw new BadRequestException("No puede crear justificaciones para un registro de otra empresa");
        }
        return registro;
    }

    private void validarEstadoPendiente(String estadoSolicitud) {
        if (!PENDIENTE.equalsIgnoreCase(estadoSolicitud)) {
            throw new BadRequestException("La justificación ya fue procesada y no puede modificarse nuevamente");
        }
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

    private boolean esEstadoJustificable(String estadoEntrada) {
        if (estadoEntrada == null) {
            return false;
        }
        return "RETARDO".equalsIgnoreCase(estadoEntrada)
                || "FALTA_INJUSTIFICADA".equalsIgnoreCase(estadoEntrada)
                || "FALTA_JUSTIFICADA".equalsIgnoreCase(estadoEntrada);
    }

    private String comentarios(JustificacionRevisionRequest request) {
        return request == null ? null : request.comentariosAdministrador();
    }

    private JustificacionResponse toResponse(BackupIncidenciasJustificacione justificacion) {
        RegistroAsistencia registro = justificacion.getRegistroAsistencia();
        return new JustificacionResponse(
                justificacion.getId(),
                registro.getId(),
                registro.getEmpleado().getId(),
                registro.getEmpleado().getNombreCompleto(),
                registro.getFecha(),
                justificacion.getMotivoEmpleado(),
                justificacion.getUrlComprobanteS3(),
                justificacion.getEstadoSolicitud(),
                justificacion.getComentariosAdministrador(),
                justificacion.getProcesadoEn(),
                justificacion.getCreadoEn()
        );
    }
}



