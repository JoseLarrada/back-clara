package com.proyecto.version1.Features.Vacaciones.service.impl;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Vacaciones.SolicitudesVacacione;
import com.proyecto.version1.Features.Vacaciones.repository.SolicitudesVacacionesRepository;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesResponse;
import com.proyecto.version1.security.TenantContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VacacionesServiceImplTest {

    @Test
    void aprobarSolicitudDebeDescontarSaldoAutomaticamente() {
        SolicitudesVacacionesRepository solicitudesRepository = mock(SolicitudesVacacionesRepository.class);
        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        VacacionesServiceImpl service = new VacacionesServiceImpl(solicitudesRepository, empleadosRepository);

        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();
        UUID solicitudId = UUID.randomUUID();
        Empleado empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Ana Pérez")
                .saldoVacaciones(10)
                .activo(true)
                .build();
        SolicitudesVacacione solicitud = SolicitudesVacacione.builder()
                .id(solicitudId)
                .empleado(empleado)
                .fechaInicio(LocalDate.of(2026, 6, 1))
                .fechaFin(LocalDate.of(2026, 6, 3))
                .estadoSolicitud("PENDIENTE")
                .creadoEn(OffsetDateTime.now())
                .build();

        TenantContext.setCurrentTenant(empresaId);
        when(solicitudesRepository.findByIdAndEmpleado_EmpresaId(solicitudId, empresaId)).thenReturn(Optional.of(solicitud));
        when(solicitudesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(empleadosRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try {
            VacacionesResponse response = service.aprobarSolicitud(solicitudId);

            assertEquals("APROBADO", response.estadoSolicitud());
            assertEquals(10, response.saldoVacacionesAntes());
            assertEquals(7, response.saldoVacacionesDespues());
            verify(empleadosRepository).save(any());
            verify(solicitudesRepository).save(any());
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void aprobarSolicitudDebeFallarSiNoExisteSaldoSuficiente() {
        SolicitudesVacacionesRepository solicitudesRepository = mock(SolicitudesVacacionesRepository.class);
        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        VacacionesServiceImpl service = new VacacionesServiceImpl(solicitudesRepository, empleadosRepository);

        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();
        UUID solicitudId = UUID.randomUUID();
        Empleado empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Ana Pérez")
                .saldoVacaciones(1)
                .activo(true)
                .build();
        SolicitudesVacacione solicitud = SolicitudesVacacione.builder()
                .id(solicitudId)
                .empleado(empleado)
                .fechaInicio(LocalDate.of(2026, 6, 1))
                .fechaFin(LocalDate.of(2026, 6, 3))
                .estadoSolicitud("PENDIENTE")
                .build();

        TenantContext.setCurrentTenant(empresaId);
        when(solicitudesRepository.findByIdAndEmpleado_EmpresaId(solicitudId, empresaId)).thenReturn(Optional.of(solicitud));

        try {
            assertThrows(BadRequestException.class, () -> service.aprobarSolicitud(solicitudId));
        } finally {
            TenantContext.clear();
        }
    }
}


