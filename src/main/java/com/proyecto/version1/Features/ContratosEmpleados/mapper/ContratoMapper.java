package com.proyecto.version1.Features.ContratosEmpleados.mapper;

import com.proyecto.version1.Features.ContratosEmpleados.ContratosEmpleado;
import com.proyecto.version1.Features.ContratosEmpleados.dto.ContratoResponse;

public class ContratoMapper {
    
    public static ContratoResponse toResponse(ContratosEmpleado contrato) {
        if (contrato == null) {
            return null;
        }
        return new ContratoResponse(
                contrato.getId(),
                contrato.getEmpleado().getId(),
                contrato.getEmpleado().getNombreCompleto(),
                contrato.getSalarioBaseMensual(),
                contrato.getTipoMoneda(),
                contrato.getTipoContrato(),
                contrato.getFechaIngreso(),
                contrato.getFechaRetiro(),
                contrato.getActivo()
        );
    }
}
