package com.proyecto.version1.Features.RecargosConfiguracion.mapper;

import com.proyecto.version1.Features.RecargosConfiguracion.ConfiguracionRecargosEmpresa;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosCreateRequest;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosResponse;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracionRecargosMapper {

    public ConfiguracionRecargosEmpresa toEntity(ConfiguracionRecargosCreateRequest request) {
        return ConfiguracionRecargosEmpresa.builder()
                .factorHoraExtraDiurna(request.getFactorHoraExtraDiurna())
                .factorHoraExtraNocturna(request.getFactorHoraExtraNocturna())
                .factorHoraDominicalFestiva(request.getFactorHoraDominicalFestiva())
                .multaRetardoPorMinuto(request.getMultaRetardoPorMinuto())
                .build();
    }

    public ConfiguracionRecargosResponse toResponse(ConfiguracionRecargosEmpresa entity) {
        return ConfiguracionRecargosResponse.builder()
                .id(entity.getId())
                .factorHoraExtraDiurna(entity.getFactorHoraExtraDiurna())
                .factorHoraExtraNocturna(entity.getFactorHoraExtraNocturna())
                .factorHoraDominicalFestiva(entity.getFactorHoraDominicalFestiva())
                .multaRetardoPorMinuto(entity.getMultaRetardoPorMinuto())
                .build();
    }

    public void applyUpdate(ConfiguracionRecargosUpdateRequest request, ConfiguracionRecargosEmpresa entity) {
        entity.setFactorHoraExtraDiurna(request.getFactorHoraExtraDiurna());
        entity.setFactorHoraExtraNocturna(request.getFactorHoraExtraNocturna());
        entity.setFactorHoraDominicalFestiva(request.getFactorHoraDominicalFestiva());
        entity.setMultaRetardoPorMinuto(request.getMultaRetardoPorMinuto());
    }
}

