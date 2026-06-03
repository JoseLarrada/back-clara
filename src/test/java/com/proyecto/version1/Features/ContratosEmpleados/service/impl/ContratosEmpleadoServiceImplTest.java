package com.proyecto.version1.Features.ContratosEmpleados.service.impl;

import com.proyecto.version1.Features.Auditoria.AuditoriaService;
import com.proyecto.version1.Features.ContratosEmpleados.ContratosEmpleado;
import com.proyecto.version1.Features.ContratosEmpleados.ContratosEmpleadoRepository;
import com.proyecto.version1.Features.ContratosEmpleados.dto.ContratoCloseRequest;
import com.proyecto.version1.Features.ContratosEmpleados.dto.ContratoCreateRequest;
import com.proyecto.version1.Features.ContratosEmpleados.dto.ContratoExtendRequest;
import com.proyecto.version1.Features.ContratosEmpleados.dto.ContratoResponse;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.security.TenantContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContratosEmpleadoServiceImplTest {

    @Test
    void crearContratoDebeCrearExitosamente() {
        ContratosEmpleadoRepository contratosRepository = mock(ContratosEmpleadoRepository.class);
        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        AuditoriaService auditoriaService = mock(AuditoriaService.class);
        ContratosEmpleadoServiceImpl service = new ContratosEmpleadoServiceImpl(contratosRepository, empleadosRepository, auditoriaService);

        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();
        Empleado empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Carlos Gómez")
                .activo(true)
                .build();

        ContratoCreateRequest request = new ContratoCreateRequest(
                empleadoId,
                new BigDecimal("1500000"),
                "COP",
                "TERMINO_INDEFINIDO",
                LocalDate.of(2026, 1, 1),
                null,
                true
        );

        TenantContext.setCurrentTenant(empresaId);
        when(empleadosRepository.findByIdAndEmpresaId(empleadoId, empresaId)).thenReturn(Optional.of(empleado));
        when(contratosRepository.existsByEmpleado_IdAndActivoTrue(empleadoId)).thenReturn(false);
        when(contratosRepository.save(any())).thenAnswer(invocation -> {
            ContratosEmpleado c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        try {
            ContratoResponse response = service.crearContrato(request);

            assertNotNull(response.id());
            assertEquals(empleadoId, response.empleadoId());
            assertTrue(response.activo());
            assertEquals(new BigDecimal("1500000"), response.salarioBaseMensual());
            verify(contratosRepository).save(any());
            verify(auditoriaService).registrarLog(eq("CREAR_CONTRATO_EMPLEADO"), eq("contratos_empleados"), any(), isNull(), any());
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void crearContratoDebeFallarSiYaHayOtroContratoActivo() {
        ContratosEmpleadoRepository contratosRepository = mock(ContratosEmpleadoRepository.class);
        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        AuditoriaService auditoriaService = mock(AuditoriaService.class);
        ContratosEmpleadoServiceImpl service = new ContratosEmpleadoServiceImpl(contratosRepository, empleadosRepository, auditoriaService);

        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();
        Empleado empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Carlos Gómez")
                .activo(true)
                .build();

        ContratoCreateRequest request = new ContratoCreateRequest(
                empleadoId,
                new BigDecimal("1500000"),
                "COP",
                "TERMINO_INDEFINIDO",
                LocalDate.of(2026, 1, 1),
                null,
                true
        );

        TenantContext.setCurrentTenant(empresaId);
        when(empleadosRepository.findByIdAndEmpresaId(empleadoId, empresaId)).thenReturn(Optional.of(empleado));
        when(contratosRepository.existsByEmpleado_IdAndActivoTrue(empleadoId)).thenReturn(true);

        try {
            assertThrows(BadRequestException.class, () -> service.crearContrato(request));
            verify(contratosRepository, never()).save(any());
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void cerrarContratoDebeDesactivarYEstablecerFechaRetiro() {
        ContratosEmpleadoRepository contratosRepository = mock(ContratosEmpleadoRepository.class);
        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        AuditoriaService auditoriaService = mock(AuditoriaService.class);
        ContratosEmpleadoServiceImpl service = new ContratosEmpleadoServiceImpl(contratosRepository, empleadosRepository, auditoriaService);

        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();
        UUID contratoId = UUID.randomUUID();
        Empleado empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Carlos Gómez")
                .activo(true)
                .build();

        ContratosEmpleado contrato = ContratosEmpleado.builder()
                .id(contratoId)
                .empresaId(empresaId)
                .empleado(empleado)
                .salarioBaseMensual(new BigDecimal("1500000"))
                .tipoMoneda("COP")
                .tipoContrato("TERMINO_INDEFINIDO")
                .fechaIngreso(LocalDate.of(2026, 1, 1))
                .activo(true)
                .build();

        ContratoCloseRequest request = new ContratoCloseRequest(LocalDate.of(2026, 6, 30));

        TenantContext.setCurrentTenant(empresaId);
        when(contratosRepository.findById(contratoId)).thenReturn(Optional.of(contrato));
        when(contratosRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try {
            ContratoResponse response = service.cerrarContrato(contratoId, request);

            assertFalse(response.activo());
            assertEquals(LocalDate.of(2026, 6, 30), response.fechaRetiro());
            verify(contratosRepository).save(any());
            verify(auditoriaService).registrarLog(eq("CERRAR_CONTRATO_EMPLEADO"), eq("contratos_empleados"), eq(contratoId), any(), any());
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    void extenderContratoDebeActualizarFechaRetiroYSalario() {
        ContratosEmpleadoRepository contratosRepository = mock(ContratosEmpleadoRepository.class);
        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        AuditoriaService auditoriaService = mock(AuditoriaService.class);
        ContratosEmpleadoServiceImpl service = new ContratosEmpleadoServiceImpl(contratosRepository, empleadosRepository, auditoriaService);

        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();
        UUID contratoId = UUID.randomUUID();
        Empleado empleado = Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Carlos Gómez")
                .activo(true)
                .build();

        ContratosEmpleado contrato = ContratosEmpleado.builder()
                .id(contratoId)
                .empresaId(empresaId)
                .empleado(empleado)
                .salarioBaseMensual(new BigDecimal("1500000"))
                .tipoMoneda("COP")
                .tipoContrato("TERMINO_FIJO")
                .fechaIngreso(LocalDate.of(2026, 1, 1))
                .fechaRetiro(LocalDate.of(2026, 6, 30))
                .activo(true)
                .build();

        ContratoExtendRequest request = new ContratoExtendRequest(LocalDate.of(2026, 12, 31), new BigDecimal("1800000"));

        TenantContext.setCurrentTenant(empresaId);
        when(contratosRepository.findById(contratoId)).thenReturn(Optional.of(contrato));
        when(contratosRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try {
            ContratoResponse response = service.extenderContrato(contratoId, request);

            assertTrue(response.activo());
            assertEquals(LocalDate.of(2026, 12, 31), response.fechaRetiro());
            assertEquals(new BigDecimal("1800000"), response.salarioBaseMensual());
            verify(contratosRepository).save(any());
            verify(auditoriaService).registrarLog(eq("EXTENDER_CONTRATO_EMPLEADO"), eq("contratos_empleados"), eq(contratoId), any(), any());
        } finally {
            TenantContext.clear();
        }
    }
}
