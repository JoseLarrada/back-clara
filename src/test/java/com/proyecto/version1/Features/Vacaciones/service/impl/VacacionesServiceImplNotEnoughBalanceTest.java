package com.proyecto.version1.Features.Vacaciones.service.impl;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Vacaciones.SolicitudesVacacione;
import com.proyecto.version1.Features.Vacaciones.repository.SolicitudesVacacionesRepository;
import com.proyecto.version1.security.TenantContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import com.proyecto.version1.Features.Auditoria.AuditoriaService;
import com.proyecto.version1.Features.Vacaciones.repository.MovimientoVacacionesRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VacacionesServiceImplNotEnoughBalanceTest {

    @Test
    void aprobarSolicitudDebeFallarCuandoElSaldoEsInsuficiente() {
        SolicitudesVacacionesRepository solicitudesRepository = mock(SolicitudesVacacionesRepository.class);
        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        MovimientoVacacionesRepository movimientoRepository = mock(MovimientoVacacionesRepository.class);
        AuditoriaService auditoriaService = mock(AuditoriaService.class);
        VacacionesServiceImpl service = new VacacionesServiceImpl(solicitudesRepository, empleadosRepository, movimientoRepository, auditoriaService);

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
        when(movimientoRepository.getSaldoVacaciones(empleadoId)).thenReturn(1);
        when(solicitudesRepository.findByIdAndEmpleado_EmpresaId(solicitudId, empresaId)).thenReturn(Optional.of(solicitud));
        when(solicitudesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try {
            assertThrows(BadRequestException.class, () -> service.aprobarSolicitud(solicitudId));
        } finally {
            TenantContext.clear();
        }
    }
}


