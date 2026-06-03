package com.proyecto.version1.Features.Geolocalizacion;

import com.proyecto.version1.Features.Empleados.Empleado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UbicacionServiceTest {

    @Mock
    private UltimaUbicacionRepository ultimaUbicacionRepository;

    @Mock
    private HistorialUbicacionRepository historialUbicacionRepository;

    @Mock
    private UbicacionWebSocketHandler webSocketHandler;

    @InjectMocks
    private UbicacionServiceImpl ubicacionService;

    private Empleado empleado;
    private UbicacionPingRequest pingRequest;

    @BeforeEach
    void setUp() {
        empleado = Empleado.builder()
                .id(UUID.randomUUID())
                .nombreCompleto("Juan Perez")
                .empresaId(UUID.randomUUID())
                .build();

        UbicacionPingRequest.PingItem item = new UbicacionPingRequest.PingItem(
                new BigDecimal("4.60971"),
                new BigDecimal("-74.08175"),
                new BigDecimal("10.0"),
                new BigDecimal("0.5"),
                new BigDecimal("180.0"),
                OffsetDateTime.now()
        );

        pingRequest = new UbicacionPingRequest(List.of(item));
    }

    @Test
    void testRegistrarUbicaciones() {
        // Arrange
        when(ultimaUbicacionRepository.findById(empleado.getId())).thenReturn(Optional.empty());

        // Act
        ubicacionService.registrarUbicaciones(empleado, pingRequest);

        // Assert
        verify(historialUbicacionRepository, times(1)).saveAll(anyList());
        verify(ultimaUbicacionRepository, times(1)).save(any(UltimaUbicacion.class));
        verify(webSocketHandler, times(1)).broadcastUbicacion(any(UbicacionResponse.class));
    }
}
