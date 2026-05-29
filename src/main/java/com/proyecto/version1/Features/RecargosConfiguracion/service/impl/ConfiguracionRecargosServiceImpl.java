package com.proyecto.version1.Features.RecargosConfiguracion.service.impl;

import com.proyecto.version1.Features.RecargosConfiguracion.ConfiguracionRecargosEmpresa;
import com.proyecto.version1.Features.RecargosConfiguracion.ConfiguracionRecargosEmpresaRepository;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosCreateRequest;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosResponse;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosUpdateRequest;
import com.proyecto.version1.Features.RecargosConfiguracion.mapper.ConfiguracionRecargosMapper;
import com.proyecto.version1.Features.RecargosConfiguracion.service.ConfiguracionRecargosService;
import com.proyecto.version1.Features.Empresas.Empresa;
import com.proyecto.version1.Features.Empresas.repository.EmpresaRepository;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfiguracionRecargosServiceImpl implements ConfiguracionRecargosService {

    private final ConfiguracionRecargosEmpresaRepository configuracionRecargosRepository;
    private final ConfiguracionRecargosMapper configuracionRecargosMapper;
    private final EmpresaRepository empresaRepository;

    @Override
    public ConfiguracionRecargosResponse crear(ConfiguracionRecargosCreateRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant no identificado en token JWT.");
        }

        // Validar que los factores sean coherentes
        if (request.getFactorHoraExtraDiurna().signum() <= 0) {
            throw new IllegalArgumentException("El factor de hora extra diurna debe ser mayor a 0");
        }
        if (request.getFactorHoraExtraNocturna().signum() <= 0) {
            throw new IllegalArgumentException("El factor de hora extra nocturna debe ser mayor a 0");
        }
        if (request.getFactorHoraDominicalFestiva().signum() <= 0) {
            throw new IllegalArgumentException("El factor de hora dominical/festiva debe ser mayor a 0");
        }
        if (request.getMultaRetardoPorMinuto().signum() < 0) {
            throw new IllegalArgumentException("La multa por retardo no puede ser negativa");
        }

        ConfiguracionRecargosEmpresa entity = configuracionRecargosMapper.toEntity(request);
        Empresa empresa = empresaRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada para el tenant autenticado."));
        entity.setEmpresa(empresa);
        ConfiguracionRecargosEmpresa saved = configuracionRecargosRepository.save(entity);
        return configuracionRecargosMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionRecargosResponse obtener(UUID id) {
        ConfiguracionRecargosEmpresa entity = configuracionRecargosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de recargos no encontrada con ID: " + id));
        return configuracionRecargosMapper.toResponse(entity);
    }

    @Override
    public ConfiguracionRecargosResponse actualizar(UUID id, ConfiguracionRecargosUpdateRequest request) {
        // Validar que los factores sean coherentes
        if (request.getFactorHoraExtraDiurna().signum() <= 0) {
            throw new IllegalArgumentException("El factor de hora extra diurna debe ser mayor a 0");
        }
        if (request.getFactorHoraExtraNocturna().signum() <= 0) {
            throw new IllegalArgumentException("El factor de hora extra nocturna debe ser mayor a 0");
        }
        if (request.getFactorHoraDominicalFestiva().signum() <= 0) {
            throw new IllegalArgumentException("El factor de hora dominical/festiva debe ser mayor a 0");
        }
        if (request.getMultaRetardoPorMinuto().signum() < 0) {
            throw new IllegalArgumentException("La multa por retardo no puede ser negativa");
        }

        ConfiguracionRecargosEmpresa entity = configuracionRecargosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de recargos no encontrada con ID: " + id));

        configuracionRecargosMapper.applyUpdate(request, entity);
        ConfiguracionRecargosEmpresa updated = configuracionRecargosRepository.save(entity);
        return configuracionRecargosMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionRecargosResponse obtenerPorEmpresa(UUID empresaId) {
        return configuracionRecargosRepository.findByEmpresa_Id(empresaId)
                .map(configuracionRecargosMapper::toResponse)
                .orElseGet(() -> ConfiguracionRecargosResponse.builder()
                        .factorHoraExtraDiurna(BigDecimal.valueOf(1.25))
                        .factorHoraExtraNocturna(BigDecimal.valueOf(1.75))
                        .factorHoraDominicalFestiva(BigDecimal.valueOf(2.00))
                        .multaRetardoPorMinuto(BigDecimal.ZERO)
                        .build());
    }

    @Override
    public void eliminar(UUID id) {
        ConfiguracionRecargosEmpresa entity = configuracionRecargosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración de recargos no encontrada con ID: " + id));
        configuracionRecargosRepository.delete(entity);
    }
}

