package com.proyecto.version1.Features.RecargosConfiguracion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionRecargosResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("factor_hora_extra_diurna")
    private BigDecimal factorHoraExtraDiurna;

    @JsonProperty("factor_hora_extra_nocturna")
    private BigDecimal factorHoraExtraNocturna;

    @JsonProperty("factor_hora_dominical_festiva")
    private BigDecimal factorHoraDominicalFestiva;

    @JsonProperty("multa_retardo_por_minuto")
    private BigDecimal multaRetardoPorMinuto;
}

