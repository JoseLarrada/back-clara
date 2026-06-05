package com.proyecto.version1.Features;

import com.proyecto.version1.Features.Anomalias.AdminAnomaliasController;
import com.proyecto.version1.Features.Anomalias.AnomaliasGravesAuditoria;
import com.proyecto.version1.Features.Anomalias.AnomaliasGravesAuditoriaRepository;
import com.proyecto.version1.Features.Anomalias.dto.ResolverAnomaliaRequest;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.Features.GeocercasRemota.AdminGeocercaReubicarController;
import com.proyecto.version1.Features.GeocercasRemota.GeocercasRemota;
import com.proyecto.version1.Features.GeocercasRemota.dto.ReubicarGeocercaRequest;
import com.proyecto.version1.Features.GeocercasRemota.repository.GeocercasRemotaRepository;
import com.proyecto.version1.Features.RegistrosAsistencias.AdminAsistenciaController;
import com.proyecto.version1.Features.RegistrosAsistencias.RegistroAsistencia;
import com.proyecto.version1.Features.RegistrosAsistencias.repository.RegistroAsistenciaRepository;
import com.proyecto.version1.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminDashboardSecurityEndpointsTest {

    private UUID tenantId;
    private UUID empleadoId;
    private Empleado empleado;

    // Repositories
    private RegistroAsistenciaRepository registroAsistenciaRepository;
    private EmpleadosRepository empleadosRepository;
    private GeocercasRemotaRepository geocercasRemotaRepository;
    private AnomaliasGravesAuditoriaRepository anomaliasGravesAuditoriaRepository;

    // Controllers
    private AdminAsistenciaController adminAsistenciaController;
    private AdminGeocercaReubicarController adminGeocercaReubicarController;
    private AdminAnomaliasController adminAnomaliasController;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        empleadoId = UUID.randomUUID();

        TenantContext.setCurrentTenant(tenantId);

        // Mocks
        registroAsistenciaRepository = mock(RegistroAsistenciaRepository.class);
        empleadosRepository = mock(EmpleadosRepository.class);
        geocercasRemotaRepository = mock(GeocercasRemotaRepository.class);
        anomaliasGravesAuditoriaRepository = mock(AnomaliasGravesAuditoriaRepository.class);

        // Controllers instantiation
        adminAsistenciaController = new AdminAsistenciaController(registroAsistenciaRepository, empleadosRepository);
        adminGeocercaReubicarController = new AdminGeocercaReubicarController(geocercasRemotaRepository, empleadosRepository);
        adminAnomaliasController = new AdminAnomaliasController(anomaliasGravesAuditoriaRepository);

        empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(tenantId)
                .nombreCompleto("Empleado Test")
                .activo(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // --- ENDPOINT 1: Forzar Salida Administrativa ---

    @Test
    void forzarSalidaDebeCerrarJornadaCorrectamente() {
        // Mock finding empleado
        when(empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId))
                .thenReturn(Optional.of(empleado));

        // Mock open attendance records
        RegistroAsistencia openAsistencia = RegistroAsistencia.builder()
                .id(UUID.randomUUID())
                .empleado(empleado)
                .tipoRegistro("ENTRADA")
                .build();

        when(registroAsistenciaRepository.findByEmpresa_IdAndEmpleado_IdAndHoraSalidaIsNullOrderByFechaDesc(tenantId, empleadoId))
                .thenReturn(List.of(openAsistencia));

        // Invoke endpoint
        ResponseEntity<Map<String, Object>> response = adminAsistenciaController.forzarSalida(empleadoId);

        // Verify response
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Turno cerrado administrativamente con éxito.", response.getBody().get("message"));

        // Verify that save was called and updated properties
        ArgumentCaptor<RegistroAsistencia> captor = ArgumentCaptor.forClass(RegistroAsistencia.class);
        verify(registroAsistenciaRepository, times(1)).save(captor.capture());

        RegistroAsistencia saved = captor.getValue();
        assertNotNull(saved.getHoraSalida());
        assertEquals("SALIDA", saved.getTipoRegistro());
        assertNotNull(saved.getInstanteServidorUltimaMarcacion());
    }

    @Test
    void forzarSalidaDebeLanzarExcepcionSiNoHayJornadaAbierta() {
        when(empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId))
                .thenReturn(Optional.of(empleado));

        when(registroAsistenciaRepository.findByEmpresa_IdAndEmpleado_IdAndHoraSalidaIsNullOrderByFechaDesc(tenantId, empleadoId))
                .thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> adminAsistenciaController.forzarSalida(empleadoId));
        verify(registroAsistenciaRepository, never()).save(any());
    }

    @Test
    void forzarSalidaDebeLanzarExcepcionSiEmpleadoNoExiste() {
        when(empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminAsistenciaController.forzarSalida(empleadoId));
        verify(registroAsistenciaRepository, never()).save(any());
    }

    // --- ENDPOINT 2: Reubicar Geocerca ---

    @Test
    void reubicarGeocercaDebeCrearNuevaGeocercaSiNoExiste() {
        when(empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId))
                .thenReturn(Optional.of(empleado));

        when(geocercasRemotaRepository.findByEmpleado_IdAndEmpleado_EmpresaId(empleadoId, tenantId))
                .thenReturn(Collections.emptyList());

        BigDecimal testLat = new BigDecimal("4.6097");
        BigDecimal testLng = new BigDecimal("-74.0817");
        ReubicarGeocercaRequest request = new ReubicarGeocercaRequest(testLat, testLng);

        ResponseEntity<Map<String, Object>> response = adminGeocercaReubicarController.reubicarGeocerca(empleadoId, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue((Boolean) response.getBody().get("success"));

        ArgumentCaptor<GeocercasRemota> captor = ArgumentCaptor.forClass(GeocercasRemota.class);
        verify(geocercasRemotaRepository, times(1)).save(captor.capture());

        GeocercasRemota saved = captor.getValue();
        assertEquals(tenantId, saved.getEmpresaId());
        assertEquals(empleado, saved.getEmpleado());
        assertEquals(testLat, saved.getLatitud());
        assertEquals(testLng, saved.getLongitud());
        assertEquals("Casa / Home Office", saved.getDescripcion());
        assertEquals(50, saved.getRadioToleranciaMetros());
    }

    @Test
    void reubicarGeocercaDebeActualizarGeocercaExistente() {
        when(empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId))
                .thenReturn(Optional.of(empleado));

        GeocercasRemota existingGeocerca = GeocercasRemota.builder()
                .id(UUID.randomUUID())
                .empresaId(tenantId)
                .empleado(empleado)
                .descripcion("Casa / Home Office")
                .latitud(new BigDecimal("1.0000"))
                .longitud(new BigDecimal("1.0000"))
                .radioToleranciaMetros(50)
                .build();

        when(geocercasRemotaRepository.findByEmpleado_IdAndEmpleado_EmpresaId(empleadoId, tenantId))
                .thenReturn(List.of(existingGeocerca));

        BigDecimal testLat = new BigDecimal("4.6097");
        BigDecimal testLng = new BigDecimal("-74.0817");
        ReubicarGeocercaRequest request = new ReubicarGeocercaRequest(testLat, testLng);

        ResponseEntity<Map<String, Object>> response = adminGeocercaReubicarController.reubicarGeocerca(empleadoId, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue((Boolean) response.getBody().get("success"));

        ArgumentCaptor<GeocercasRemota> captor = ArgumentCaptor.forClass(GeocercasRemota.class);
        verify(geocercasRemotaRepository, times(1)).save(captor.capture());

        GeocercasRemota saved = captor.getValue();
        assertEquals(existingGeocerca.getId(), saved.getId());
        assertEquals(testLat, saved.getLatitud());
        assertEquals(testLng, saved.getLongitud());
    }

    // --- ENDPOINT 3: Resolver Anomalía ---

    @Test
    void resolverAnomaliaDebeActualizarCamposExitosamente() {
        UUID anomaliaId = UUID.randomUUID();
        AnomaliasGravesAuditoria anomalia = AnomaliasGravesAuditoria.builder()
                .id(anomaliaId)
                .empresaId(tenantId)
                .tipoAnomalia("MOCK_LOCATION")
                .estado("PENDIENTE")
                .detallesTecnicos("GPS simulado detectado.")
                .build();

        when(anomaliasGravesAuditoriaRepository.findByIdAndEmpresaId(anomaliaId, tenantId))
                .thenReturn(Optional.of(anomalia));

        ResolverAnomaliaRequest request = new ResolverAnomaliaRequest("Justificado por visita a cliente.", "RESOLVIDO");

        ResponseEntity<Map<String, Object>> response = adminAnomaliasController.resolverAnomalia(anomaliaId, request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Anomalía registrada y archivada como resuelta.", response.getBody().get("message"));

        ArgumentCaptor<AnomaliasGravesAuditoria> captor = ArgumentCaptor.forClass(AnomaliasGravesAuditoria.class);
        verify(anomaliasGravesAuditoriaRepository, times(1)).save(captor.capture());

        AnomaliasGravesAuditoria saved = captor.getValue();
        assertEquals("RESOLVIDO", saved.getEstado());
        assertEquals("Justificado por visita a cliente.", saved.getComentario());
    }

    @Test
    void resolverAnomaliaDebeLanzarExcepcionSiNoExiste() {
        UUID anomaliaId = UUID.randomUUID();
        when(anomaliasGravesAuditoriaRepository.findByIdAndEmpresaId(anomaliaId, tenantId))
                .thenReturn(Optional.empty());

        ResolverAnomaliaRequest request = new ResolverAnomaliaRequest("Justificado.", "RESOLVIDO");

        assertThrows(ResourceNotFoundException.class, () -> adminAnomaliasController.resolverAnomalia(anomaliaId, request));
        verify(anomaliasGravesAuditoriaRepository, never()).save(any());
    }
}
