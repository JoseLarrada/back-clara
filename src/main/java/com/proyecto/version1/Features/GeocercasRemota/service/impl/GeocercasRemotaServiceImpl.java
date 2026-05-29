package com.proyecto.version1.Features.GeocercasRemota.service.impl;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.Features.GeocercasRemota.GeocercasRemota;
import com.proyecto.version1.Features.GeocercasRemota.dto.GeocercaRemotaRequest;
import com.proyecto.version1.Features.GeocercasRemota.dto.GeocercaRemotaResponse;
import com.proyecto.version1.Features.GeocercasRemota.repository.GeocercasRemotaRepository;
import com.proyecto.version1.Features.GeocercasRemota.service.GeocercasRemotaService;
import com.proyecto.version1.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GeocercasRemotaServiceImpl implements GeocercasRemotaService {

    private final GeocercasRemotaRepository geocercasRemotaRepository;
    private final EmpleadosRepository empleadosRepository;

    @Override
    public GeocercaRemotaResponse crearGeocerca(GeocercaRemotaRequest request) {
        UUID tenantId = requireTenant();
        Empleado empleado = requireEmpleadoTenant(request.empleadoId(), tenantId);
        validarDescripcion(request.descripcion());
        validarRadio(request.radioToleranciaMetros());

        GeocercasRemota entity = GeocercasRemota.builder()
                .empleado(empleado)
                .descripcion(request.descripcion())
                .latitud(request.latitud())
                .longitud(request.longitud())
                .radioToleranciaMetros(request.radioToleranciaMetros())
                .build();

        return toResponse(geocercasRemotaRepository.save(entity));
    }

    @Override
    public GeocercaRemotaResponse actualizarGeocerca(UUID id, GeocercaRemotaRequest request) {
        UUID tenantId = requireTenant();
        Empleado empleado = requireEmpleadoTenant(request.empleadoId(), tenantId);
        validarDescripcion(request.descripcion());
        validarRadio(request.radioToleranciaMetros());

        GeocercasRemota entity = geocercasRemotaRepository.findByIdAndEmpleado_EmpresaId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Geocerca remota no encontrada para la empresa autenticada."));

        entity.setEmpleado(empleado);
        entity.setDescripcion(request.descripcion());
        entity.setLatitud(request.latitud());
        entity.setLongitud(request.longitud());
        entity.setRadioToleranciaMetros(request.radioToleranciaMetros());

        return toResponse(geocercasRemotaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public GeocercaRemotaResponse obtenerGeocerca(UUID id) {
        UUID tenantId = requireTenant();
        GeocercasRemota entity = geocercasRemotaRepository.findByIdAndEmpleado_EmpresaId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Geocerca remota no encontrada para la empresa autenticada."));
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeocercaRemotaResponse> listarGeocercas(UUID empleadoId) {
        UUID tenantId = requireTenant();

        if (empleadoId != null) {
            requireEmpleadoTenant(empleadoId, tenantId);
            return geocercasRemotaRepository.findByEmpleado_IdAndEmpleado_EmpresaId(empleadoId, tenantId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return geocercasRemotaRepository.findByEmpleado_EmpresaId(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void eliminarGeocerca(UUID id) {
        UUID tenantId = requireTenant();
        GeocercasRemota entity = geocercasRemotaRepository.findByIdAndEmpleado_EmpresaId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Geocerca remota no encontrada para la empresa autenticada."));
        geocercasRemotaRepository.delete(entity);
    }

    private UUID requireTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no identificado en token JWT.");
        }
        return tenantId;
    }

    private Empleado requireEmpleadoTenant(UUID empleadoId, UUID tenantId) {
        return empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa autenticada."));
    }

    private void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new BadRequestException("La descripcion es obligatoria.");
        }
    }

    private void validarRadio(Integer radioToleranciaMetros) {
        if (radioToleranciaMetros == null || radioToleranciaMetros <= 0) {
            throw new BadRequestException("El radio de tolerancia debe ser mayor que 0.");
        }
    }

    private GeocercaRemotaResponse toResponse(GeocercasRemota entity) {
        return new GeocercaRemotaResponse(
                entity.getId(),
                entity.getEmpleado().getId(),
                entity.getDescripcion(),
                entity.getLatitud(),
                entity.getLongitud(),
                entity.getRadioToleranciaMetros()
        );
    }
}

