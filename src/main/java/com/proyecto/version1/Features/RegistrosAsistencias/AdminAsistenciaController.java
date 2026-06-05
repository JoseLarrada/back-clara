package com.proyecto.version1.Features.RegistrosAsistencias;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.Features.RegistrosAsistencias.repository.RegistroAsistenciaRepository;
import com.proyecto.version1.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/asistencia")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH', 'SUPERADMIN')")
@RequiredArgsConstructor
@Tag(name = "Asistencia Admin", description = "Operaciones administrativas de control de asistencia")
public class AdminAsistenciaController {

    private final RegistroAsistenciaRepository registroAsistenciaRepository;
    private final EmpleadosRepository empleadosRepository;

    @PostMapping("/forzar-salida/{empleadoId}")
    @Operation(summary = "Forzar Salida Administrativa", description = "Marca la salida de un colaborador que haya olvidado registrar su ponche de salida, cerrando su turno.")
    public ResponseEntity<Map<String, Object>> forzarSalida(@PathVariable UUID empleadoId) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new BadRequestException("Tenant no identificado en la sesión.");
        }

        // Verificar existencia del empleado y pertenencia al tenant
        Empleado empleado = empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa actual."));

        // Buscar jornada abierta (horaSalida es null) ordenada por fecha desc
        List<RegistroAsistencia> openJornadas = registroAsistenciaRepository
                .findByEmpresa_IdAndEmpleado_IdAndHoraSalidaIsNullOrderByFechaDesc(tenantId, empleadoId);

        if (openJornadas.isEmpty()) {
            throw new BadRequestException("No se encontró una jornada abierta activa para este empleado.");
        }

        // Tomar la jornada abierta más reciente y cerrarla
        RegistroAsistencia registro = openJornadas.get(0);
        OffsetDateTime ahora = OffsetDateTime.now(ZoneId.systemDefault());
        registro.setHoraSalida(ahora);
        registro.setTipoRegistro("SALIDA");
        registro.setInstanteServidorUltimaMarcacion(ahora);

        registroAsistenciaRepository.save(registro);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Turno cerrado administrativamente con éxito."
        ));
    }
}
