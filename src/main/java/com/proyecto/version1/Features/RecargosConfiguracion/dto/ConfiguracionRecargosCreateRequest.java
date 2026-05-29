package com.proyecto.version1.Features.RecargosConfiguracion.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionRecargosCreateRequest {

    @NotNull(message = "El factor de hora extra diurna es requerido")
    @DecimalMin(value = "1.0", inclusive = true, message = "El factor debe ser mayor o igual a 1.0")
    @DecimalMax(value = "3.0", inclusive = true, message = "El factor no puede exceder 3.0")
    private BigDecimal factorHoraExtraDiurna;

    @NotNull(message = "El factor de hora extra nocturna es requerido")
    @DecimalMin(value = "1.0", inclusive = true, message = "El factor debe ser mayor o igual a 1.0")
    @DecimalMax(value = "3.0", inclusive = true, message = "El factor no puede exceder 3.0")
    private BigDecimal factorHoraExtraNocturna;

    @NotNull(message = "El factor de hora dominical/festiva es requerido")
    @DecimalMin(value = "1.0", inclusive = true, message = "El factor debe ser mayor o igual a 1.0")
    @DecimalMax(value = "3.0", inclusive = true, message = "El factor no puede exceder 3.0")
    private BigDecimal factorHoraDominicalFestiva;

    @NotNull(message = "La multa por retardo por minuto es requerida")
    @DecimalMin(value = "0.0", inclusive = true, message = "La multa no puede ser negativa")
    @DecimalMax(value = "999999.99", inclusive = true, message = "La multa está fuera del rango permitido")
    private BigDecimal multaRetardoPorMinuto;
}

