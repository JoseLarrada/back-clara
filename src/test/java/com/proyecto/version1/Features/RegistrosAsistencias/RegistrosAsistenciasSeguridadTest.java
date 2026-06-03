package com.proyecto.version1.Features.RegistrosAsistencias;

import com.proyecto.version1.Features.Calendario.repository.CalendarioHibridoRepository;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.dto.OrigenMarcacionAsistencia;
import com.proyecto.version1.Features.Empleados.dto.RegistrarAsistenciaRequest;
import com.proyecto.version1.Features.Empleados.dto.RegistroAsistenciaResponse;
import com.proyecto.version1.Features.Empleados.dto.TipoMarcacionAsistencia;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empleados.service.impl.EmpleadoPanelServiceImpl;
import com.proyecto.version1.Features.Empresas.Empresa;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.repository.EmpresaRepository;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorario;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorarioRepository;
import com.proyecto.version1.Features.RegistrosAsistencias.repository.RegistroAsistenciaRepository;
import com.proyecto.version1.Features.GeocercasRemota.GeocercasRemota;
import com.proyecto.version1.Features.GeocercasRemota.repository.GeocercasRemotaRepository;
import com.proyecto.version1.Features.Anomalias.service.AlertaService;
import com.proyecto.version1.Features.RegistrosAsistencias.service.QrValidationService;
import com.proyecto.version1.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegistrosAsistenciasSeguridadTest {

    private UUID empresaId;
    private UUID empleadoId;
    private Empleado empleado;
    private Empresa empresa;
    private ReglaHorario regla;

    private EmpleadosRepository empleadosRepository;
    private EmpresaRepository empresaRepository;
    private RegistroAsistenciaRepository registroRepository;
    private ReglaHorarioRepository reglaRepository;
    private CalendarioHibridoRepository calendarioRepository;
    private AlertaService alertaService;
    private QrValidationService qrValidationService;
    private GeocercasRemotaRepository geocercasRemotaRepository;

    private EmpleadoPanelServiceImpl service;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        empleadoId = UUID.randomUUID();

        empleadosRepository = mock(EmpleadosRepository.class);
        empresaRepository = mock(EmpresaRepository.class);
        registroRepository = mock(RegistroAsistenciaRepository.class);
        reglaRepository = mock(ReglaHorarioRepository.class);
        calendarioRepository = mock(CalendarioHibridoRepository.class);
        alertaService = mock(AlertaService.class);
        qrValidationService = mock(QrValidationService.class);
        geocercasRemotaRepository = mock(GeocercasRemotaRepository.class);

        empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Carlos Gomez")
                .email("carlos@test.com")
                .modalidadPerfil("REMOTO")
                .activo(true)
                .build();

        empresa = Empresa.builder()
                .id(empresaId)
                .nombre("Test Company")
                .nitRut("999999-9")
                .rubro("Tecnologia")
                .build();

        regla = ReglaHorario.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaId)
                .descripcion("Jornada Normal")
                .horaEntradaOficial(LocalTime.of(8, 0))
                .horaSalidaOficial(LocalTime.of(17, 0))
                .minutosToleranciaRetardo(15)
                .tiempoLimiteFaltaMinutos(120)
                .build();

        TenantContext.setCurrentTenant(empresaId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("carlos@test.com", null, List.of(new SimpleGrantedAuthority("EMPLEADO")))
        );

        when(empleadosRepository.findByEmailIgnoreCase("carlos@test.com")).thenReturn(Optional.of(empleado));
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(reglaRepository.findByEmpresaId(empresaId)).thenReturn(List.of(regla));
        when(registroRepository.findByEmpresa_IdAndEmpleado_IdAndFecha(eq(empresaId), eq(empleadoId), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        service = new EmpleadoPanelServiceImpl(
                empleadosRepository,
                empresaRepository,
                registroRepository,
                reglaRepository,
                calendarioRepository,
                alertaService,
                qrValidationService,
                geocercasRemotaRepository
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarAsistenciaDebeRechazarMockLocationYDispararAlerta() {
        RegistrarAsistenciaRequest request = new RegistrarAsistenciaRequest(
                TipoMarcacionAsistencia.ENTRADA,
                OrigenMarcacionAsistencia.BOTON_REMOTO,
                null,
                true,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(4.6097),
                BigDecimal.valueOf(-74.0817),
                true, // Mock location activa!
                "http://s3.aws/foto.jpg",
                95.0
        );

        assertThrows(BadRequestException.class, () -> service.registrarAsistencia(request));
        verify(alertaService, times(1)).registrarYDispararAlerta(eq(empleado), eq("MOCK_LOCATION_DETECTADA"), anyString());
    }

    @Test
    void registrarAsistenciaDebeRechazarFaceMismatchYDispararAlerta() {
        RegistrarAsistenciaRequest request = new RegistrarAsistenciaRequest(
                TipoMarcacionAsistencia.ENTRADA,
                OrigenMarcacionAsistencia.BOTON_REMOTO,
                null,
                false, // Falla facial
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(4.6097),
                BigDecimal.valueOf(-74.0817),
                false,
                "http://s3.aws/foto.jpg",
                65.0 // Score por debajo de 80
        );

        assertThrows(BadRequestException.class, () -> service.registrarAsistencia(request));
        verify(alertaService, times(1)).registrarYDispararAlerta(eq(empleado), eq("FACE_MISMATCH"), anyString());
    }

    @Test
    void registrarAsistenciaDebeRechazarFueraDeGeocercaYDispararAlerta() {
        // Coordenadas geocerca: (4.6097, -74.0817) con radio de 50 metros
        GeocercasRemota geocerca = GeocercasRemota.builder()
                .empresaId(empresaId)
                .empleado(empleado)
                .descripcion("Casa")
                .latitud(BigDecimal.valueOf(4.6097))
                .longitud(BigDecimal.valueOf(-74.0817))
                .radioToleranciaMetros(50)
                .build();

        when(geocercasRemotaRepository.findByEmpleado_IdAndEmpleado_EmpresaId(empleadoId, empresaId))
                .thenReturn(List.of(geocerca));

        // Petición con coordenadas lejanas: (5.6097, -75.0817)
        RegistrarAsistenciaRequest request = new RegistrarAsistenciaRequest(
                TipoMarcacionAsistencia.ENTRADA,
                OrigenMarcacionAsistencia.BOTON_REMOTO,
                null,
                true,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5.6097),
                BigDecimal.valueOf(-75.0817),
                false,
                "http://s3.aws/foto.jpg",
                95.0
        );

        assertThrows(BadRequestException.class, () -> service.registrarAsistencia(request));
        verify(alertaService, times(1)).registrarYDispararAlerta(eq(empleado), eq("FUERA_DE_GEOCERCA"), anyString());
    }

    @Test
    void registrarAsistenciaDebePermitirEntradaRemotaCorrectaSiCumpleCondiciones() {
        GeocercasRemota geocerca = GeocercasRemota.builder()
                .empresaId(empresaId)
                .empleado(empleado)
                .descripcion("Casa")
                .latitud(BigDecimal.valueOf(4.6097))
                .longitud(BigDecimal.valueOf(-74.0817))
                .radioToleranciaMetros(50)
                .build();

        when(geocercasRemotaRepository.findByEmpleado_IdAndEmpleado_EmpresaId(empleadoId, empresaId))
                .thenReturn(List.of(geocerca));

        // Coordenadas muy cercanas (menos de 50 metros)
        RegistrarAsistenciaRequest request = new RegistrarAsistenciaRequest(
                TipoMarcacionAsistencia.ENTRADA,
                OrigenMarcacionAsistencia.BOTON_REMOTO,
                null,
                true,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(4.60971),
                BigDecimal.valueOf(-74.08171),
                false,
                "http://s3.aws/foto.jpg",
                85.5
        );

        UUID generatedId = UUID.randomUUID();
        when(registroRepository.save(any(RegistroAsistencia.class))).thenAnswer(invocation -> {
            RegistroAsistencia r = invocation.getArgument(0);
            r.setId(generatedId);
            return r;
        });

        RegistroAsistenciaResponse response = service.registrarAsistencia(request);

        assertNotNull(response);
        assertEquals(generatedId, response.id());
        assertEquals("REMOTO", response.modalidadAplicada());
        assertTrue(response.esFacialVerificado());
        assertFalse(response.esMockLocation());
        assertEquals("http://s3.aws/foto.jpg", response.fotoCapturaUrl());
        assertEquals(85.5, response.scoreFacialCoincidencia());
        verifyNoInteractions(alertaService);
    }
}
