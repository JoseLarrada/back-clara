package com.proyecto.version1.Features.Backups_Incidencias.service.impl;

import com.proyecto.version1.Features.Backups_Incidencias.BackupIncidenciasJustificacione;
import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionRevisionRequest;
import com.proyecto.version1.Features.Backups_Incidencias.repository.BackupIncidenciasJustificacionesRepository;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.Empresa;
import com.proyecto.version1.Features.RegistrosAsistencias.RegistroAsistencia;
import com.proyecto.version1.Features.RegistrosAsistencias.repository.RegistroAsistenciaRepository;
import com.proyecto.version1.security.TenantContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackupIncidenciasServiceImplTest {

    @Test
    void aprobarJustificacionDebeMarcarLaAsistenciaComoJustificada() {
        BackupIncidenciasJustificacionesRepository justificacionesRepository = mock(BackupIncidenciasJustificacionesRepository.class);
        RegistroAsistenciaRepository registroRepository = mock(RegistroAsistenciaRepository.class);
        EmpleadosRepository empleadosRepository = mock(EmpleadosRepository.class);
        BackupIncidenciasServiceImpl service = new BackupIncidenciasServiceImpl(justificacionesRepository, registroRepository, empleadosRepository);

        UUID empresaId = UUID.randomUUID();
        UUID empleadoId = UUID.randomUUID();
        UUID registroId = UUID.randomUUID();
        UUID justificacionId = UUID.randomUUID();

        Empresa empresa = Empresa.builder().id(empresaId).nombre("ACME").nitRut("123").rubro("Servicios").build();
        com.proyecto.version1.Features.Empleados.Empleado empleado = com.proyecto.version1.Features.Empleados.Empleado.builder()
                .id(empleadoId)
                .empresaId(empresaId)
                .nombreCompleto("Juan Gómez")
                .saldoVacaciones(15)
                .activo(true)
                .build();
        RegistroAsistencia registro = RegistroAsistencia.builder()
                .id(registroId)
                .empresa(empresa)
                .empleado(empleado)
                .fecha(LocalDate.of(2026, 6, 1))
                .estadoEntrada("FALTA_INJUSTIFICADA")
                .modalidadAplicada("PRESENCIAL")
                .horaEntrada(OffsetDateTime.now())
                .build();
        BackupIncidenciasJustificacione justificacion = BackupIncidenciasJustificacione.builder()
                .id(justificacionId)
                .registroAsistencia(registro)
                .motivoEmpleado("Cita médica")
                .urlComprobanteS3("https://s3.example.com/justificante.pdf")
                .estadoSolicitud("PENDIENTE")
                .build();

        TenantContext.setCurrentTenant(empresaId);
        when(justificacionesRepository.findByIdAndEmpresaId(justificacionId, empresaId)).thenReturn(Optional.of(justificacion));
        when(justificacionesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(registroRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        try {
            var response = service.aprobarJustificacion(justificacionId, new JustificacionRevisionRequest("Ok"));

            assertEquals("APROBADO", response.estadoSolicitud());
            assertEquals("Ok", response.comentariosAdministrador());
            assertEquals("FALTA_JUSTIFICADA", registro.getEstadoEntrada());
            assertNotNull(response.procesadoEn());
            verify(registroRepository).save(any());
            verify(justificacionesRepository).save(any());
        } finally {
            TenantContext.clear();
        }
    }
}


