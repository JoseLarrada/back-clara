package com.proyecto.version1.Features.Anomalias;

import com.proyecto.version1.Features.Anomalias.dto.ResolverAnomaliaRequest;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/anomalias")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH', 'SUPERADMIN')")
@RequiredArgsConstructor
@Tag(name = "Anomalías Admin", description = "Operaciones administrativas de gestión de anomalías graves de seguridad")
public class AdminAnomaliasController {

    private final AnomaliasGravesAuditoriaRepository anomaliasGravesAuditoriaRepository;

    @PostMapping("/{id}/resolver")
    @Operation(summary = "Resolver Anomalía Grave", description = "Permite registrar un comentario de resolución y cambiar el estado de una anomalía grave de seguridad.")
    public ResponseEntity<Map<String, Object>> resolverAnomalia(
            @PathVariable UUID id,
            @Valid @RequestBody ResolverAnomaliaRequest request
    ) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new BadRequestException("Tenant no identificado en la sesión.");
        }

        // Buscar la anomalía filtrando por id y tenant (empresaId)
        AnomaliasGravesAuditoria anomalia = anomaliasGravesAuditoriaRepository.findByIdAndEmpresaId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Anomalía no encontrada para la empresa actual."));

        // Actualizar estado y comentario
        anomalia.setEstado(request.estado());
        anomalia.setComentario(request.comentario());

        anomaliasGravesAuditoriaRepository.save(anomalia);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Anomalía registrada y archivada como resuelta."
        ));
    }
}
