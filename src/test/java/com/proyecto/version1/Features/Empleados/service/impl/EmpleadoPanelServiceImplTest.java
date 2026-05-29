package com.proyecto.version1.Features.Empleados.service.impl;

import com.proyecto.version1.Features.Calendario.repository.CalendarioHibridoRepository;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.dto.EstadoPanelEmpleadoResponse;
import com.proyecto.version1.Features.Empleados.dto.HistorialAsistenciaMensualResponse;
import com.proyecto.version1.Features.Empleados.dto.OrigenMarcacionAsistencia;
import com.proyecto.version1.Features.Empleados.dto.RegistrarAsistenciaRequest;
import com.proyecto.version1.Features.Empleados.dto.RegistroAsistenciaResponse;
import com.proyecto.version1.Features.Empleados.dto.TipoMarcacionAsistencia;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.Empresa;
import com.proyecto.version1.Features.Empresas.repository.EmpresaRepository;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorario;
import com.proyecto.version1.Features.ReglasHorario.ReglaHorarioRepository;
import com.proyecto.version1.Features.RegistrosAsistencias.RegistroAsistencia;
import com.proyecto.version1.Features.RegistrosAsistencias.repository.RegistroAsistenciaRepository;
import com.proyecto.version1.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmpleadoPanelServiceImplTest {

    @AfterEach
    void limpiarContexto() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void obtenerPanelDebeActivarQrParaPresencial() {
        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();

        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        RegistroAsistenciaRepository registroRepository = mock(RegistroAsistenciaRepository.class);
        ReglaHorarioRepository reglaRepository = mock(ReglaHorarioRepository.class);
        CalendarioHibridoRepository calendarioRepository = mock(CalendarioHibridoRepository.class);

        Empleado empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Juan Perez")
                .email("empleado@test.com")
                .modalidadPerfil("PRESENCIAL")
                .activo(true)
                .build();

        TenantContext.setCurrentTenant(empresaId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("empleado@test.com", null, List.of(new SimpleGrantedAuthority("EMPLEADO")))
        );

        when(empleadosRepository.findByEmailIgnoreCase("empleado@test.com")).thenReturn(Optional.of(empleado));
        when(registroRepository.findByEmpresa_IdAndEmpleado_IdAndFecha(empresaId, empleadoId, LocalDate.now())).thenReturn(Optional.empty());

        EmpleadoPanelServiceImpl service = new EmpleadoPanelServiceImpl(
                empleadosRepository,
                empresaRepository,
                registroRepository,
                reglaRepository,
                calendarioRepository
        );

        EstadoPanelEmpleadoResponse response = service.obtenerPanelEmpleado();

        assertTrue(response.requiereQr());
        assertFalse(response.requiereGps());
        assertFalse(response.puedeRegistrarSalida());
        assertEquals("SIN_REGISTRO", response.estadoLaboral());
    }

    @Test
    void registrarEntradaDebeGuardarMarcacionPresencial() {
        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();
        UUID registroId = UUID.randomUUID();

        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        RegistroAsistenciaRepository registroRepository = mock(RegistroAsistenciaRepository.class);
        ReglaHorarioRepository reglaRepository = mock(ReglaHorarioRepository.class);
        CalendarioHibridoRepository calendarioRepository = mock(CalendarioHibridoRepository.class);

        Empleado empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Juan Perez")
                .email("empleado@test.com")
                .modalidadPerfil("PRESENCIAL")
                .activo(true)
                .build();

        Empresa empresa = Empresa.builder().id(empresaId).nombre("ACME").nitRut("123").rubro("Servicios").build();
        ReglaHorario regla = ReglaHorario.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaId)
                .descripcion("Jornada normal")
                .horaEntradaOficial(LocalTime.of(8, 0))
                .horaSalidaOficial(LocalTime.of(17, 0))
                .minutosToleranciaRetardo(10)
                .tiempoLimiteFaltaMinutos(120)
                .build();

        TenantContext.setCurrentTenant(empresaId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("empleado@test.com", null, List.of(new SimpleGrantedAuthority("EMPLEADO")))
        );

        when(empleadosRepository.findByEmailIgnoreCase("empleado@test.com")).thenReturn(Optional.of(empleado));
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(reglaRepository.findByEmpresaId(empresaId)).thenReturn(List.of(regla));
        when(registroRepository.findByEmpresa_IdAndEmpleado_IdAndFecha(empresaId, empleadoId, LocalDate.now())).thenReturn(Optional.empty());
        when(registroRepository.save(any())).thenAnswer(invocation -> {
            RegistroAsistencia entity = invocation.getArgument(0);
            entity.setId(registroId);
            return entity;
        });

        EmpleadoPanelServiceImpl service = new EmpleadoPanelServiceImpl(
                empleadosRepository,
                empresaRepository,
                registroRepository,
                reglaRepository,
                calendarioRepository
        );

        RegistroAsistenciaResponse response = service.registrarAsistencia(
                new RegistrarAsistenciaRequest(TipoMarcacionAsistencia.ENTRADA, OrigenMarcacionAsistencia.QR_FISICO, null, null, null)
        );

        assertEquals(registroId, response.id());
        assertEquals(TipoMarcacionAsistencia.ENTRADA, response.tipoMarcacionRegistrada());
        assertEquals(OrigenMarcacionAsistencia.QR_FISICO, response.origenMarcacion());
        assertTrue(response.mensaje().contains("Entrada"));
        assertNotNull(response.horaEntrada());
    }

    @Test
    void obtenerPanelDebeMostrarEstadoEnAlmuerzoYCronometro() {
        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();
        UUID registroId = UUID.randomUUID();

        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        RegistroAsistenciaRepository registroRepository = mock(RegistroAsistenciaRepository.class);
        ReglaHorarioRepository reglaRepository = mock(ReglaHorarioRepository.class);
        CalendarioHibridoRepository calendarioRepository = mock(CalendarioHibridoRepository.class);

        Empleado empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Juan Perez")
                .email("empleado@test.com")
                .modalidadPerfil("PRESENCIAL")
                .activo(true)
                .build();

        OffsetDateTime entrada = OffsetDateTime.now().minusHours(3);
        OffsetDateTime inicioAlmuerzo = OffsetDateTime.now().minusMinutes(20);
        RegistroAsistencia registro = RegistroAsistencia.builder()
                .id(registroId)
                .empleado(empleado)
                .fecha(LocalDate.now())
                .horaEntrada(entrada)
                .horaAlmuerzoInicio(inicioAlmuerzo)
                .horaAlmuerzoFin(null)
                .estadoEntrada("A_TIEMPO")
                .modalidadAplicada("PRESENCIAL")
                .tipoRegistro("ALMUERZO")
                .instanteServidorUltimaMarcacion(inicioAlmuerzo)
                .build();

        TenantContext.setCurrentTenant(empresaId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("empleado@test.com", null, List.of(new SimpleGrantedAuthority("EMPLEADO")))
        );

        when(empleadosRepository.findByEmailIgnoreCase("empleado@test.com")).thenReturn(Optional.of(empleado));
        when(registroRepository.findByEmpresa_IdAndEmpleado_IdAndFecha(empresaId, empleadoId, LocalDate.now())).thenReturn(Optional.of(registro));

        EmpleadoPanelServiceImpl service = new EmpleadoPanelServiceImpl(
                empleadosRepository,
                empresaRepository,
                registroRepository,
                reglaRepository,
                calendarioRepository
        );

        EstadoPanelEmpleadoResponse response = service.obtenerPanelEmpleado();

        assertEquals("EN_ALMUERZO", response.estadoLaboral());
        assertTrue(response.cronometroJornadaSegundos() > 0);
        assertTrue(response.cronometroAlmuerzoSegundos() > 0);
        assertTrue(response.cronometroTrabajoNetoSegundos() >= 0);
    }

    @Test
    void registrarAlmuerzoDebeActualizarRegistroExistente() {
        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();
        UUID registroId = UUID.randomUUID();

        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        RegistroAsistenciaRepository registroRepository = mock(RegistroAsistenciaRepository.class);
        ReglaHorarioRepository reglaRepository = mock(ReglaHorarioRepository.class);
        CalendarioHibridoRepository calendarioRepository = mock(CalendarioHibridoRepository.class);

        Empleado empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Juan Perez")
                .email("empleado@test.com")
                .modalidadPerfil("PRESENCIAL")
                .activo(true)
                .build();

        Empresa empresa = Empresa.builder().id(empresaId).nombre("ACME").nitRut("123").rubro("Servicios").build();
        ReglaHorario regla = ReglaHorario.builder()
                .id(UUID.randomUUID())
                .empresaId(empresaId)
                .descripcion("Jornada normal")
                .horaEntradaOficial(LocalTime.of(8, 0))
                .horaSalidaOficial(LocalTime.of(17, 0))
                .minutosToleranciaRetardo(10)
                .tiempoLimiteFaltaMinutos(120)
                .build();

        RegistroAsistencia existente = RegistroAsistencia.builder()
                .id(registroId)
                .empresa(empresa)
                .empleado(empleado)
                .fecha(LocalDate.now())
                .horaEntrada(OffsetDateTime.now().minusHours(2))
                .modalidadAplicada("PRESENCIAL")
                .estadoEntrada("A_TIEMPO")
                .tipoRegistro("ENTRADA")
                .build();

        TenantContext.setCurrentTenant(empresaId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("empleado@test.com", null, List.of(new SimpleGrantedAuthority("EMPLEADO")))
        );

        when(empleadosRepository.findByEmailIgnoreCase("empleado@test.com")).thenReturn(Optional.of(empleado));
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(reglaRepository.findByEmpresaId(empresaId)).thenReturn(List.of(regla));
        when(registroRepository.findByEmpresa_IdAndEmpleado_IdAndFecha(empresaId, empleadoId, LocalDate.now())).thenReturn(Optional.of(existente));
        when(registroRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmpleadoPanelServiceImpl service = new EmpleadoPanelServiceImpl(
                empleadosRepository,
                empresaRepository,
                registroRepository,
                reglaRepository,
                calendarioRepository
        );

        RegistroAsistenciaResponse response = service.registrarAsistencia(
                new RegistrarAsistenciaRequest(TipoMarcacionAsistencia.ALMUERZO, OrigenMarcacionAsistencia.QR_FISICO, null, null, null)
        );

        assertEquals(TipoMarcacionAsistencia.ALMUERZO, response.tipoMarcacionRegistrada());
        assertEquals("ALMUERZO", response.tipoRegistroPersistido());
        assertNotNull(response.horaAlmuerzoInicio());
        assertTrue(response.mensaje().contains("almuerzo"));
    }

    @Test
    void historialMensualDebeConsolidarAsistenciasRetardosFaltasYHoras() {
        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();

        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        EmpresaRepository empresaRepository = mock(EmpresaRepository.class);
        RegistroAsistenciaRepository registroRepository = mock(RegistroAsistenciaRepository.class);
        ReglaHorarioRepository reglaRepository = mock(ReglaHorarioRepository.class);
        CalendarioHibridoRepository calendarioRepository = mock(CalendarioHibridoRepository.class);

        Empleado empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Juan Perez")
                .email("empleado@test.com")
                .modalidadPerfil("PRESENCIAL")
                .activo(true)
                .build();

        LocalDate hoy = LocalDate.now();
        int anio = hoy.getYear();
        int mes = hoy.getMonthValue();

        RegistroAsistencia dia1 = RegistroAsistencia.builder()
                .id(UUID.randomUUID())
                .empleado(empleado)
                .fecha(LocalDate.of(anio, mes, 1))
                .horaEntrada(OffsetDateTime.now().minusHours(10))
                .horaAlmuerzoInicio(OffsetDateTime.now().minusHours(6))
                .horaAlmuerzoFin(OffsetDateTime.now().minusHours(5))
                .horaSalida(OffsetDateTime.now().minusHours(2))
                .estadoEntrada("A_TIEMPO")
                .modalidadAplicada("PRESENCIAL")
                .tipoRegistro("SALIDA")
                .instanteServidorUltimaMarcacion(OffsetDateTime.now().minusHours(2))
                .build();

        RegistroAsistencia dia2 = RegistroAsistencia.builder()
                .id(UUID.randomUUID())
                .empleado(empleado)
                .fecha(LocalDate.of(anio, mes, 2))
                .horaEntrada(OffsetDateTime.now().minusHours(9))
                .horaSalida(OffsetDateTime.now().minusHours(1))
                .estadoEntrada("RETARDO")
                .modalidadAplicada("PRESENCIAL")
                .tipoRegistro("SALIDA")
                .instanteServidorUltimaMarcacion(OffsetDateTime.now().minusHours(1))
                .build();

        TenantContext.setCurrentTenant(empresaId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("empleado@test.com", null, List.of(new SimpleGrantedAuthority("EMPLEADO")))
        );

        when(empleadosRepository.findByEmailIgnoreCase("empleado@test.com")).thenReturn(Optional.of(empleado));
        when(registroRepository.findByEmpresa_IdAndEmpleado_IdAndFechaBetween(
                any(), any(), any(), any())).thenReturn(List.of(dia1, dia2));

        EmpleadoPanelServiceImpl service = new EmpleadoPanelServiceImpl(
                empleadosRepository,
                empresaRepository,
                registroRepository,
                reglaRepository,
                calendarioRepository
        );

        HistorialAsistenciaMensualResponse response = service.obtenerHistorialMensual(anio, mes);

        assertEquals(empleadoId, response.empleadoId());
        assertTrue(response.totalAsistencias() >= 2);
        assertTrue(response.totalRetardos() >= 1);
        assertTrue(response.totalHorasTrabajadasSegundos() > 0);
        assertTrue(response.totalHorasNetasSegundos() >= 0);
        assertFalse(response.calendario().isEmpty());
    }
}


