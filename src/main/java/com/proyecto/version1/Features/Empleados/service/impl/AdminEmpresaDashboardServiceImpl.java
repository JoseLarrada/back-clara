package com.proyecto.version1.Features.Empleados.service.impl;

import com.proyecto.version1.Features.Empleados.dto.AdminDashboardTiempoRealResponse;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empleados.service.AdminEmpresaDashboardService;
import com.proyecto.version1.Features.RegistrosAsistencias.repository.RegistroAsistenciaRepository;
import com.proyecto.version1.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEmpresaDashboardServiceImpl implements AdminEmpresaDashboardService {

    private final EmpleadosRepository empleadosRepository;
    private final RegistroAsistenciaRepository registroAsistenciaRepository;

    @Override
    public AdminDashboardTiempoRealResponse obtenerDashboardTiempoReal(LocalDate fecha) {
        UUID empresaId = TenantContext.getCurrentTenant();
        if (empresaId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no identificado en el token JWT.");
        }

        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();

        long totalActivos = empleadosRepository.countByEmpresaIdAndActivoTrue(empresaId);

        long totalRegistrosDia = registroAsistenciaRepository.countByEmpresa_IdAndFecha(empresaId, fechaConsulta);
        long totalRetardos = registroAsistenciaRepository.countByEmpresa_IdAndFechaAndEstadoEntradaIn(
                empresaId,
                fechaConsulta,
                List.of("RETARDO")
        );
        long totalFaltas = registroAsistenciaRepository.countByEmpresa_IdAndFechaAndEstadoEntradaIn(
                empresaId,
                fechaConsulta,
                List.of("FALTA_JUSTIFICADA", "FALTA_INJUSTIFICADA")
        );

        long totalTeletrabajo = registroAsistenciaRepository.countByEmpresa_IdAndFechaAndModalidadAplicada(
                empresaId,
                fechaConsulta,
                "REMOTO"
        );

        long totalPresencial = registroAsistenciaRepository.countByEmpresa_IdAndFechaAndModalidadAplicada(
                empresaId,
                fechaConsulta,
                "PRESENCIAL"
        );

        // Criterio operativo: entre 12:00 y 14:00, empleado presencial con entrada y sin salida.
        long totalEnAlmuerzo = 0;
        LocalTime now = LocalTime.now();
        if (!now.isBefore(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(14, 0))) {
            totalEnAlmuerzo = registroAsistenciaRepository
                    .countByEmpresa_IdAndFechaAndHoraSalidaIsNullAndModalidadAplicada(empresaId, fechaConsulta, "PRESENCIAL");
        }

        long totalPresentes = Math.max(totalRegistrosDia - totalFaltas, 0);
        long totalAusentes = Math.max(totalActivos - totalPresentes, 0);

        return new AdminDashboardTiempoRealResponse(
                fechaConsulta,
                totalActivos,
                totalPresentes,
                totalAusentes,
                totalTeletrabajo,
                totalPresencial,
                totalEnAlmuerzo,
                totalRetardos,
                totalFaltas
        );
    }
}

