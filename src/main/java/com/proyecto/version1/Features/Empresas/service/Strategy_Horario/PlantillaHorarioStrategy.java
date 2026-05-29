package com.proyecto.version1.Features.Empresas.service.Strategy_Horario;

import java.util.UUID;

public interface PlantillaHorarioStrategy {
    String getRubroSoportado();
    void precargarHorariosPorDefecto(UUID empresaId);
}
