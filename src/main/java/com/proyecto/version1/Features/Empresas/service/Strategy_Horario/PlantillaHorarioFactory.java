package com.proyecto.version1.Features.Empresas.service.Strategy_Horario;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PlantillaHorarioFactory {
    private final Map<String, PlantillaHorarioStrategy> strategies;

    // Spring Boot inyecta automáticamente todas las clases que implementen la interfaz
    public PlantillaHorarioFactory(List<PlantillaHorarioStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getRubroSoportado().toUpperCase(),
                        Function.identity()
                ));
    }

    public PlantillaHorarioStrategy getStrategy(String rubro) {
        PlantillaHorarioStrategy strategy = strategies.get(rubro.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("El rubro '" + rubro + "' no tiene una estrategia de horarios configurada.");
        }
        return strategy;
    }
}
