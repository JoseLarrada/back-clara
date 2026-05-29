package com.proyecto.version1.Features.Empresas.service.Strategy_Horario;

import com.proyecto.version1.Features.ReglasHorario.ReglaHorario;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TecnologiaPlantillaStrategy implements PlantillaHorarioStrategy {
    private final ReglaHorarioRepository reglaHorarioRepository;

    private static final int TIEMPO_LIMITE_FALTA = 120;

    @Override
    public String getRubroSoportado() {
        return "TECNOLOGIA";
    }

    @Override
    public void precargarHorariosPorDefecto(UUID empresaId) {
        ReglaHorario horarioTI = ReglaHorario.builder()
                .empresaId(empresaId)
                .descripcion("Horario Flexible TI")
                .horaEntradaOficial(LocalTime.of(8, 0))
                .horaSalidaOficial(LocalTime.of(17, 0))
                .minutosToleranciaRetardo(30)
                .tiempoLimiteFaltaMinutos(TIEMPO_LIMITE_FALTA)
                .build();

        reglaHorarioRepository.save(horarioTI);
    }
}
