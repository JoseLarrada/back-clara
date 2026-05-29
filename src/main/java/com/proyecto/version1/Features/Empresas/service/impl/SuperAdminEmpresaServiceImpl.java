package com.proyecto.version1.Features.Empresas.service.impl;

import com.proyecto.version1.Features.Empresas.Empresa;
import com.proyecto.version1.Features.Empresas.EstadoLicencia;
import com.proyecto.version1.Features.Empresas.dto.*;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.Empresas.exception.DuplicateResourceException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.Features.Empresas.mapper.EmpresaMapper;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.repository.EmpresaRepository;
import com.proyecto.version1.Features.Empresas.repository.EmpresaSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.proyecto.version1.Features.Empresas.service.SuperAdminEmpresaService;
import com.proyecto.version1.Features.Empresas.service.Strategy_Horario.PlantillaHorarioFactory;
import com.proyecto.version1.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@Transactional
@RequiredArgsConstructor
public class SuperAdminEmpresaServiceImpl implements SuperAdminEmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;
    private final PlantillaHorarioFactory plantillaHorarioFactory;
    private final EmpleadosRepository empleadosRepository;

    private static final int DEFAULT_LIMITE_EMPLEADOS = 50;

    private <T> T runAsGlobal(Supplier<T> supplier) {
        UUID previous = TenantContext.getCurrentTenant();
        TenantContext.clear();
        try {
            return supplier.get();
        } finally {
            if (previous != null) {
                TenantContext.setCurrentTenant(previous);
            }
        }
    }

    @Override
    public ResponseEntity<ApiResponse<EmpresaResponse>> crearEmpresa(EmpresaCreateRequest request) {
        return runAsGlobal(() -> {
            if (empresaRepository.existsByNitRut(request.nitRut())) {
                throw new DuplicateResourceException("Ya existe una empresa con el NIT/RUT proporcionado: " + request.nitRut());
            }

            Empresa entity = empresaMapper.toEntity(request);

            if (entity.getLimiteEmpleados() == null) {
                entity.setLimiteEmpleados(DEFAULT_LIMITE_EMPLEADOS);
            }

            if (entity.getEstadoLicencia() == null || entity.getEstadoLicencia().isBlank()) {
                entity.setEstadoLicencia(EstadoLicencia.ACTIVO.name());
            }

            OffsetDateTime now = OffsetDateTime.now();
            entity.setCreadoEn(now);
            entity.setActualizadoEn(now);

            Empresa saved = empresaRepository.save(entity);

            // RF03: precargar plantillas de horario según rubro
            try {
                plantillaHorarioFactory.getStrategy(saved.getRubro()).precargarHorariosPorDefecto(saved.getId());
                EmpresaResponse resp = empresaMapper.toResponse(saved);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(true, "Empresa creada correctamente.", resp));
            } catch (DataAccessException dae) {
                // Persistimos la empresa, pero la tabla de reglas no existe o fallo SQL
                EmpresaResponse resp = empresaMapper.toResponse(saved);
                String msg = "Empresa creada, pero fallo al precargar plantillas de horario: " + dae.getMostSpecificCause().getMessage();
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(true, msg, resp));
            } catch (Exception ex) {
                EmpresaResponse resp = empresaMapper.toResponse(saved);
                String msg = "Empresa creada, pero fallo al precargar plantillas de horario: " + ex.getMessage();
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(true, msg, resp));
            }
        });
    }

    @Override
    public ResponseEntity<ApiResponse<EmpresaResponse>> actualizarEmpresa(UUID id, EmpresaUpdateRequest request) {
        return runAsGlobal(() -> {
            Empresa entity = empresaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + id));

            empresaMapper.updateFromUpdateRequest(request, entity);
            entity.setActualizadoEn(OffsetDateTime.now());

            Empresa updated = empresaRepository.save(entity);
            return ResponseEntity.ok(new ApiResponse<>(true, "Empresa actualizada correctamente.", empresaMapper.toResponse(updated)));
        });
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> eliminarEmpresa(UUID id) {
        return runAsGlobal(() -> {
            Empresa entity = empresaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + id));
            empresaRepository.delete(entity);
            return ResponseEntity.ok(new ApiResponse<>(true, "Empresa eliminada correctamente.", null));
        });
    }

    @Override
    public ResponseEntity<ApiResponse<EmpresaResponse>> obtenerPorId(UUID id) {
        return runAsGlobal(() -> empresaRepository.findById(id)
                .map(e -> ResponseEntity.ok(new ApiResponse<>(true, "OK", empresaMapper.toResponse(e))))
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + id)));
    }

    @Override
    public ResponseEntity<ApiResponse<List<EmpresaResponse>>> listarEmpresas() {
        return runAsGlobal(() -> ResponseEntity.ok(new ApiResponse<>(true, "OK", empresaMapper.toResponseList(empresaRepository.findAll()))));
    }

    @Override
    public ResponseEntity<ApiResponse<EmpresaResponse>> actualizarEstadoLicencia(UUID id, EstadoLicencia nuevoEstado) {
        return runAsGlobal(() -> {
            Empresa entity = empresaRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + id));

            entity.setEstadoLicencia(nuevoEstado.name());
            entity.setActualizadoEn(OffsetDateTime.now());

            Empresa saved = empresaRepository.save(entity);
            return ResponseEntity.ok(new ApiResponse<>(true, "Estado de licencia actualizado.", empresaMapper.toResponse(saved)));
        });
    }

    @Override
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> obtenerMetricasGlobales() {
        return runAsGlobal(() -> {
            long empresasActivas = empresaRepository.countByEstadoLicencia(EstadoLicencia.ACTIVO.name());
            long empleadosGlobales = empleadosRepository.countAllEmpleados();
            return ResponseEntity.ok(new ApiResponse<>(true, "Métricas obtenidas.", new DashboardMetricsResponse(empresasActivas, empleadosGlobales)));
        });
    }

    @Override
    public ResponseEntity<ApiResponse<PageResponse<EmpresaResponse>>> listarEmpresasConFiltros(
            String nombre,
            String nitRut,
            String rubro,
            String estadoLicencia,
            Pageable pageable) {
        return runAsGlobal(() -> {
            // Combinamos especificaciones dinámicamente usando Specification.where()
            Specification<Empresa> spec = Specification
                    .where(EmpresaSpecifications.conNombreLike(nombre))
                    .and(EmpresaSpecifications.conNitRut(nitRut))
                    .and(EmpresaSpecifications.conRubro(rubro))
                    .and(EmpresaSpecifications.conEstadoLicencia(estadoLicencia));

            // Ejecutamos la consulta paginada
            Page<Empresa> page = empresaRepository.findAll(spec, pageable);

            // Mapeamos a DTO y construimos PageResponse
            PageResponse<EmpresaResponse> pageResponse = new PageResponse<>(
                    empresaMapper.toResponseList(page.getContent()),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Empresas listadas correctamente.", pageResponse));
        });
    }
}


