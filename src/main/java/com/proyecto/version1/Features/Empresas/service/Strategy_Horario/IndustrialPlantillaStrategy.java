package com.proyecto.version1.Features.Empresas.service.Strategy_Horario;

import com.proyecto.version1.Features.ReglasHorario.ReglaHorario;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IndustrialPlantillaStrategy implements PlantillaHorarioStrategy {
    private final ReglaHorarioRepository reglaHorarioRepository;

    private static final int TIEMPO_LIMITE_FALTA = 120;

    @Override
    public String getRubroSoportado() {
        return "INDUSTRIAL";
    }

    @Override
    public void precargarHorariosPorDefecto(UUID empresaId) {
        // Persistimos 3 turnos rotativos para la empresa
        ReglaHorario turnoManana = ReglaHorario.builder()
                .empresaId(empresaId)
                .descripcion("Turno Mañana")
                .horaEntradaOficial(LocalTime.of(6, 0))
                .horaSalidaOficial(LocalTime.of(14, 0))
                .minutosToleranciaRetardo(5)
                .tiempoLimiteFaltaMinutos(TIEMPO_LIMITE_FALTA)
                .build();

        ReglaHorario turnoTarde = ReglaHorario.builder()
                .empresaId(empresaId)
                .descripcion("Turno Tarde")
                .horaEntradaOficial(LocalTime.of(14, 0))
                .horaSalidaOficial(LocalTime.of(22, 0))
                .minutosToleranciaRetardo(5)
                .tiempoLimiteFaltaMinutos(TIEMPO_LIMITE_FALTA)
                .build();

        ReglaHorario turnoNoche = ReglaHorario.builder()
                .empresaId(empresaId)
                .descripcion("Turno Noche")
                .horaEntradaOficial(LocalTime.of(22, 0))
                .horaSalidaOficial(LocalTime.of(6, 0))
                .minutosToleranciaRetardo(5)
                .tiempoLimiteFaltaMinutos(TIEMPO_LIMITE_FALTA)
                .build();

        reglaHorarioRepository.save(turnoManana);
        reglaHorarioRepository.save(turnoTarde);
        reglaHorarioRepository.save(turnoNoche);
    }
}
