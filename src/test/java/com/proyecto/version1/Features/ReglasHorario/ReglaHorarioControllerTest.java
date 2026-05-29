package com.proyecto.version1.Features.ReglasHorario;

import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioResponse;
import com.proyecto.version1.Features.ReglasHorario.service.ReglaHorarioService;
import com.proyecto.version1.security.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReglaHorarioControllerTest {

    @Test
    void listarDebeRetornarLasReglasDeLaEmpresaAutenticada() {
        ReglaHorarioService service = mock(ReglaHorarioService.class);
        ReglaHorarioController controller = new ReglaHorarioController(service);
        UUID empresaId = UUID.randomUUID();
        ReglaHorarioResponse response = ReglaHorarioResponse.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaId)
                .descripcion("Turno estándar")
                .horaEntradaOficial(LocalTime.of(8, 0))
                .horaSalidaOficial(LocalTime.of(17, 0))
                .minutosToleranciaRetardo(10)
                .tiempoLimiteFaltaMinutos(120)
                .build();

        TenantContext.setCurrentTenant(empresaId);
        when(service.listarPorEmpresa(empresaId)).thenReturn(List.of(response));

        try {
            ResponseEntity<List<ReglaHorarioResponse>> result = controller.listar();

            assertEquals(200, result.getStatusCode().value());
            assertSame(response, result.getBody().get(0));
        } finally {
            TenantContext.clear();
        }
    }
}


