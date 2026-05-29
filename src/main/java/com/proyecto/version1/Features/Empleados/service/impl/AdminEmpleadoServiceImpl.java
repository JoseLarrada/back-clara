package com.proyecto.version1.Features.Empleados.service.impl;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoCreateRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoFotoRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoModalidadLoteRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoModalidadLoteResponse;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoModalidadRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoResponse;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoUpdateRequest;
import com.proyecto.version1.Features.Empleados.mapper.AdminEmpleadoMapper;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empleados.service.AdminEmpleadoService;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.DuplicateResourceException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminEmpleadoServiceImpl implements AdminEmpleadoService {

    private static final Set<String> ROLES_VALIDOS = Set.of("SUPERADMIN", "ADMIN_RRHH", "EMPLEADO");
    private static final Set<String> MODALIDADES_VALIDAS = Set.of("PRESENCIAL", "HIBRIDO", "REMOTO");

    private final EmpleadosRepository empleadosRepository;
    private final AdminEmpleadoMapper adminEmpleadoMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminEmpleadoResponse crearEmpleado(AdminEmpleadoCreateRequest request) {
        UUID tenantId = requireTenant();

        validarRol(request.rol());
        validarModalidad(request.modalidadPerfil());

        if (empleadosRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Ya existe un empleado con el email: " + request.email());
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Empleado entity = adminEmpleadoMapper.toEntity(request, tenantId, encodedPassword);
        Empleado saved = empleadosRepository.save(entity);
        return adminEmpleadoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminEmpleadoResponse> listarEmpleados(Pageable pageable) {
        UUID tenantId = requireTenant();
        Page<Empleado> page = empleadosRepository.findByEmpresaIdAndRol(tenantId, "EMPLEADO", pageable);

        return new PageResponse<>(
                page.map(adminEmpleadoMapper::toResponse).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminEmpleadoResponse obtenerEmpleado(UUID empleadoId) {
        UUID tenantId = requireTenant();
        Empleado entity = empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa autenticada."));
        return adminEmpleadoMapper.toResponse(entity);
    }

    @Override
    public AdminEmpleadoResponse actualizarEmpleado(UUID empleadoId, AdminEmpleadoUpdateRequest request) {
        UUID tenantId = requireTenant();

        validarRol(request.rol());
        validarModalidad(request.modalidadPerfil());

        Empleado entity = empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa autenticada."));

        empleadosRepository.findByEmailIgnoreCase(request.email())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(entity.getId())) {
                        throw new DuplicateResourceException("El email ya esta en uso por otro empleado: " + request.email());
                    }
                });

        adminEmpleadoMapper.applyUpdate(request, entity);
        Empleado updated = empleadosRepository.save(entity);
        return adminEmpleadoMapper.toResponse(updated);
    }

    @Override
    public AdminEmpleadoResponse actualizarFotoPatron(UUID empleadoId, AdminEmpleadoFotoRequest request) {
        UUID tenantId = requireTenant();

        Empleado entity = empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa autenticada."));

        entity.setFotoPatronUrl(request.fotoPatronUrl());
        Empleado updated = empleadosRepository.save(entity);
        return adminEmpleadoMapper.toResponse(updated);
    }

    @Override
    public AdminEmpleadoResponse actualizarModalidad(UUID empleadoId, AdminEmpleadoModalidadRequest request) {
        UUID tenantId = requireTenant();

        validarModalidad(request.modalidadPerfil());

        Empleado entity = empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa autenticada."));

        entity.setModalidadPerfil(request.modalidadPerfil() != null ? request.modalidadPerfil().toUpperCase() : null);
        Empleado updated = empleadosRepository.save(entity);
        return adminEmpleadoMapper.toResponse(updated);
    }

    @Override
    public AdminEmpleadoModalidadLoteResponse actualizarModalidadLote(AdminEmpleadoModalidadLoteRequest request) {
        UUID tenantId = requireTenant();
        validarModalidad(request.modalidadPerfil());

        List<UUID> noEncontrados = new ArrayList<>();
        int actualizados = 0;

        for (UUID empleadoId : request.empleadoIds()) {
            Empleado entity = empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId).orElse(null);
            if (entity == null) {
                noEncontrados.add(empleadoId);
                continue;
            }
            entity.setModalidadPerfil(request.modalidadPerfil() != null ? request.modalidadPerfil().toUpperCase() : null);
            empleadosRepository.save(entity);
            actualizados++;
        }

        return new AdminEmpleadoModalidadLoteResponse(
                request.modalidadPerfil(),
                request.empleadoIds().size(),
                actualizados,
                noEncontrados
        );
    }

    @Override
    public void eliminarEmpleado(UUID empleadoId) {
        UUID tenantId = requireTenant();

        Empleado entity = empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa autenticada."));

        // Baja logica para mantener trazabilidad y evitar ruptura de claves foraneas.
        entity.setActivo(false);
        empleadosRepository.save(entity);
    }

    private UUID requireTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Tenant no identificado en token JWT.");
        }
        return tenantId;
    }

    private void validarRol(String rol) {
        if (rol == null || rol.isBlank() || !ROLES_VALIDOS.contains(rol.toUpperCase())) {
            throw new BadRequestException("Rol invalido. Valores permitidos: SUPERADMIN, ADMIN_RRHH, EMPLEADO.");
        }
    }

    private void validarModalidad(String modalidad) {
        if (modalidad == null || modalidad.isBlank() || !MODALIDADES_VALIDAS.contains(modalidad.toUpperCase())) {
            throw new BadRequestException("Modalidad invalida. Valores permitidos: PRESENCIAL, HIBRIDO, REMOTO.");
        }
    }
}

