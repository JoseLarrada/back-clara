package com.proyecto.version1.Features.ReglasHorario.mapper;

import com.proyecto.version1.Features.ReglasHorario.ReglaHorario;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioCreateRequest;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioResponse;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class ReglaHorarioMapper {

    public ReglaHorario toEntity(ReglaHorarioCreateRequest request) {
        return ReglaHorario.builder()
                .empresaId(request.getEmpresaId())
                .descripcion(request.getDescripcion())
                .horaEntradaOficial(request.getHoraEntradaOficial())
                .horaSalidaOficial(request.getHoraSalidaOficial())
                .minutosToleranciaRetardo(request.getMinutosToleranciaRetardo())
                .tiempoLimiteFaltaMinutos(request.getTiempoLimiteFaltaMinutos())
                .build();
    }

    public ReglaHorarioResponse toResponse(ReglaHorario entity) {
        return ReglaHorarioResponse.builder()
                .id(entity.getId())
                .empresaId(entity.getEmpresaId())
                .descripcion(entity.getDescripcion())
                .horaEntradaOficial(entity.getHoraEntradaOficial())
                .horaSalidaOficial(entity.getHoraSalidaOficial())
                .minutosToleranciaRetardo(entity.getMinutosToleranciaRetardo())
                .tiempoLimiteFaltaMinutos(entity.getTiempoLimiteFaltaMinutos())
                .build();
    }

    public void applyUpdate(ReglaHorarioUpdateRequest request, ReglaHorario entity) {
        entity.setDescripcion(request.getDescripcion());
        entity.setHoraEntradaOficial(request.getHoraEntradaOficial());
        entity.setHoraSalidaOficial(request.getHoraSalidaOficial());
        entity.setMinutosToleranciaRetardo(request.getMinutosToleranciaRetardo());
        entity.setTiempoLimiteFaltaMinutos(request.getTiempoLimiteFaltaMinutos());
    }
}

