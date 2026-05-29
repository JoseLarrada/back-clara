package com.proyecto.version1.Features.ReglasHorario.service.impl;

import com.proyecto.version1.Features.ReglasHorario.ReglaHorario;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorarioRepository;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioCreateRequest;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioResponse;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioUpdateRequest;
import com.proyecto.version1.Features.ReglasHorario.mapper.ReglaHorarioMapper;
import com.proyecto.version1.Features.ReglasHorario.service.ReglaHorarioService;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReglaHorarioServiceImpl implements ReglaHorarioService {

    private final ReglaHorarioRepository reglaHorarioRepository;
    private final ReglaHorarioMapper reglaHorarioMapper;

    @Override
    public ReglaHorarioResponse crear(ReglaHorarioCreateRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // Si no se provee el ID de empresa, se obtiene del contexto de seguridad (token JWT)
        if (request.getEmpresaId() == null) {
            request.setEmpresaId(tenantId);
        }

        // Validar que se está creando para la empresa autenticada
        if (!request.getEmpresaId().equals(tenantId)) {
            throw new IllegalArgumentException("No puede crear reglas para una empresa diferente a la autenticada");
        }

        // Validar que la hora de entrada sea anterior a la hora de salida
        if (request.getHoraEntradaOficial().isAfter(request.getHoraSalidaOficial())) {
            throw new IllegalArgumentException("La hora de entrada debe ser anterior a la hora de salida");
        }

        // Validar que tiempoLimiteFalta sea mayor que minutosTolerancia
        if (request.getTiempoLimiteFaltaMinutos() <= request.getMinutosToleranciaRetardo()) {
            throw new IllegalArgumentException("El tiempo límite de falta debe ser mayor que los minutos de tolerancia");
        }

        ReglaHorario entity = reglaHorarioMapper.toEntity(request);
        ReglaHorario saved = reglaHorarioRepository.save(entity);
        return reglaHorarioMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReglaHorarioResponse> listarPorEmpresa(UUID empresaId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // Validar que se está consultando para la empresa autenticada
        if (!empresaId.equals(tenantId)) {
            throw new IllegalArgumentException("No puede consultar reglas de una empresa diferente a la autenticada");
        }

        List<ReglaHorario> reglas = reglaHorarioRepository.findByEmpresaId(empresaId);
        return reglas.stream()
                .map(reglaHorarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReglaHorarioResponse> listarPorEmpresaConPaginacion(UUID empresaId, Pageable pageable) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // Validar que se está consultando para la empresa autenticada
        if (!empresaId.equals(tenantId)) {
            throw new IllegalArgumentException("No puede consultar reglas de una empresa diferente a la autenticada");
        }

        Page<ReglaHorario> page = reglaHorarioRepository.findByEmpresaId(empresaId, pageable);

        return new PageResponse<>(
                page.map(reglaHorarioMapper::toResponse).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReglaHorarioResponse obtener(UUID reglaHorarioId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        ReglaHorario regla = reglaHorarioRepository.findById(reglaHorarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Regla de horario no encontrada con ID: " + reglaHorarioId));

        // Validar que la regla pertenece a la empresa autenticada
        if (!regla.getEmpresaId().equals(tenantId)) {
            throw new IllegalArgumentException("No puede acceder a esta regla de horario");
        }

        return reglaHorarioMapper.toResponse(regla);
    }

    @Override
    public ReglaHorarioResponse actualizar(UUID reglaHorarioId, ReglaHorarioUpdateRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // Validar que la hora de entrada sea anterior a la hora de salida
        if (request.getHoraEntradaOficial().isAfter(request.getHoraSalidaOficial())) {
            throw new IllegalArgumentException("La hora de entrada debe ser anterior a la hora de salida");
        }

        // Validar que tiempoLimiteFalta sea mayor que minutosTolerancia
        if (request.getTiempoLimiteFaltaMinutos() <= request.getMinutosToleranciaRetardo()) {
            throw new IllegalArgumentException("El tiempo límite de falta debe ser mayor que los minutos de tolerancia");
        }

        ReglaHorario regla = reglaHorarioRepository.findById(reglaHorarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Regla de horario no encontrada con ID: " + reglaHorarioId));

        // Validar que la regla pertenece a la empresa autenticada
        if (!regla.getEmpresaId().equals(tenantId)) {
            throw new IllegalArgumentException("No puede actualizar una regla de horario de otra empresa");
        }

        reglaHorarioMapper.applyUpdate(request, regla);
        ReglaHorario updated = reglaHorarioRepository.save(regla);
        return reglaHorarioMapper.toResponse(updated);
    }

    @Override
    public void eliminar(UUID reglaHorarioId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        ReglaHorario regla = reglaHorarioRepository.findById(reglaHorarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Regla de horario no encontrada con ID: " + reglaHorarioId));

        // Validar que la regla pertenece a la empresa autenticada
        if (!regla.getEmpresaId().equals(tenantId)) {
            throw new IllegalArgumentException("No puede eliminar una regla de horario de otra empresa");
        }

        reglaHorarioRepository.delete(regla);
    }

    @Override
    @Transactional(readOnly = true)
    public ReglaHorarioResponse obtenerPorEmpresa(UUID empresaId) {
        UUID tenantId = TenantContext.getCurrentTenant();

        // Validar que se está consultando para la empresa autenticada
        if (!empresaId.equals(tenantId)) {
            throw new IllegalArgumentException("No puede consultar reglas de una empresa diferente a la autenticada");
        }

        List<ReglaHorario> reglas = reglaHorarioRepository.findByEmpresaId(empresaId);

        if (reglas.isEmpty()) {
            throw new ResourceNotFoundException("No hay reglas de horario configuradas para esta empresa");
        }

        // Retornar la primera regla (usualmente hay una por empresa)
        return reglaHorarioMapper.toResponse(reglas.getFirst());
    }
}

