package com.proyecto.version1.Features.ReglasHorario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReglaHorarioResponse {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("empresa_id")
    private UUID empresaId;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("hora_entrada_oficial")
    private LocalTime horaEntradaOficial;

    @JsonProperty("hora_salida_oficial")
    private LocalTime horaSalidaOficial;

    @JsonProperty("minutos_tolerancia_retardo")
    private Integer minutosToleranciaRetardo;

    @JsonProperty("tiempo_limite_falta_minutos")
    private Integer tiempoLimiteFaltaMinutos;
}

