package com.proyecto.version1.Features.GeocercasRemota;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.Features.GeocercasRemota.dto.ReubicarGeocercaRequest;
import com.proyecto.version1.Features.GeocercasRemota.repository.GeocercasRemotaRepository;
import com.proyecto.version1.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/empleados")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH', 'SUPERADMIN')")
@RequiredArgsConstructor
@Tag(name = "Geocercas Admin", description = "Operaciones administrativas de gestión de geocercas")
public class AdminGeocercaReubicarController {

    private final GeocercasRemotaRepository geocercasRemotaRepository;
    private final EmpleadosRepository empleadosRepository;

    @PostMapping("/{empleadoId}/geocerca/reubicar")
    @Operation(summary = "Reubicar Geocerca desde Ubicación Actual", description = "Reubica el perímetro de tolerancia del empleado a las coordenadas GPS provistas. Si no existe una geocerca, la crea.")
    public ResponseEntity<Map<String, Object>> reubicarGeocerca(
            @PathVariable UUID empleadoId,
            @Valid @RequestBody ReubicarGeocercaRequest request
    ) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new BadRequestException("Tenant no identificado en la sesión.");
        }

        // Verificar existencia del empleado y pertenencia al tenant
        Empleado empleado = empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa actual."));

        // Buscar geocercas configuradas para el empleado
        List<GeocercasRemota> geocercas = geocercasRemotaRepository
                .findByEmpleado_IdAndEmpleado_EmpresaId(empleadoId, tenantId);

        GeocercasRemota geocerca;
        if (geocercas.isEmpty()) {
            // Crear geocerca principal por defecto
            geocerca = GeocercasRemota.builder()
                    .empresaId(tenantId)
                    .empleado(empleado)
                    .descripcion("Casa / Home Office")
                    .latitud(request.latitud())
                    .longitud(request.longitud())
                    .radioToleranciaMetros(50) // valor de tolerancia por defecto en el esquema
                    .build();
        } else {
            // Tomar la primera (principal) y reubicarla
            geocerca = geocercas.get(0);
            geocerca.setLatitud(request.latitud());
            geocerca.setLongitud(request.longitud());
        }

        geocercasRemotaRepository.save(geocerca);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Geocerca principal del colaborador reubicada exitosamente."
        ));
    }
}
