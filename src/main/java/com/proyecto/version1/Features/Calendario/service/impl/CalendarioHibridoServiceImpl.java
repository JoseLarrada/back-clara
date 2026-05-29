package com.proyecto.version1.Features.Calendario.service.impl;

import com.proyecto.version1.Features.Calendario.CalendarioHibrido;
import com.proyecto.version1.Features.Calendario.dto.CalendarioHibridoLoteRequest;
import com.proyecto.version1.Features.Calendario.dto.CalendarioHibridoRequest;
import com.proyecto.version1.Features.Calendario.dto.CalendarioHibridoResponse;
import com.proyecto.version1.Features.Calendario.repository.CalendarioHibridoRepository;
import com.proyecto.version1.Features.Calendario.service.CalendarioHibridoService;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarioHibridoServiceImpl implements CalendarioHibridoService {

    private static final String PRESENCIAL = "PRESENCIAL";
    private static final String REMOTO = "REMOTO";

    private final CalendarioHibridoRepository calendarioHibridoRepository;
    private final EmpleadosRepository empleadosRepository;

    @Override
    public CalendarioHibridoResponse crearCalendario(CalendarioHibridoRequest request) {
        UUID tenantId = requireTenant();
        Empleado empleado = requireEmpleadoTenant(request.empleadoId(), tenantId);
        validarCaracterDia(request.caracterDia());

        CalendarioHibrido existente = calendarioHibridoRepository
                .findByEmpleado_IdAndFecha(empleado.getId(), request.fecha())
                .orElse(null);

        CalendarioHibrido entity = existente != null ? existente : new CalendarioHibrido();
        entity.setEmpleado(empleado);
        entity.setFecha(request.fecha());
        entity.setCaracterDia(request.caracterDia().toUpperCase());

        CalendarioHibrido saved = calendarioHibridoRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    public CalendarioHibridoResponse actualizarCalendario(UUID id, CalendarioHibridoRequest request) {
        UUID tenantId = requireTenant();
        Empleado empleado = requireEmpleadoTenant(request.empleadoId(), tenantId);
        validarCaracterDia(request.caracterDia());

        CalendarioHibrido entity = calendarioHibridoRepository.findByIdAndEmpleado_EmpresaId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendario hibrido no encontrado para la empresa autenticada."));

        entity.setEmpleado(empleado);
        entity.setFecha(request.fecha());
        entity.setCaracterDia(request.caracterDia().toUpperCase());

        return toResponse(calendarioHibridoRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public CalendarioHibridoResponse obtenerCalendario(UUID id) {
        UUID tenantId = requireTenant();
        CalendarioHibrido entity = calendarioHibridoRepository.findByIdAndEmpleado_EmpresaId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendario hibrido no encontrado para la empresa autenticada."));
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarioHibridoResponse> listarCalendarios(UUID empleadoId, LocalDate desde, LocalDate hasta) {
        UUID tenantId = requireTenant();
        LocalDate start = desde != null ? desde : LocalDate.now().withDayOfMonth(1);
        LocalDate end = hasta != null ? hasta : start.plusMonths(1).minusDays(1);

        if (empleadoId != null) {
            requireEmpleadoTenant(empleadoId, tenantId);
            return calendarioHibridoRepository.findByEmpleado_IdAndFechaBetween(empleadoId, start, end)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return calendarioHibridoRepository.findByEmpleado_EmpresaIdAndFechaBetween(tenantId, start, end)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<CalendarioHibridoResponse> upsertCalendarioLote(CalendarioHibridoLoteRequest request) {
        UUID tenantId = requireTenant();
        Empleado empleado = requireEmpleadoTenant(request.empleadoId(), tenantId);

        List<CalendarioHibridoResponse> resultados = new ArrayList<>();
        for (CalendarioHibridoRequest item : request.asignaciones()) {
            validarCaracterDia(item.caracterDia());

            CalendarioHibrido entity = calendarioHibridoRepository
                    .findByEmpleado_IdAndFecha(empleado.getId(), item.fecha())
                    .orElseGet(CalendarioHibrido::new);

            entity.setEmpleado(empleado);
            entity.setFecha(item.fecha());
            entity.setCaracterDia(item.caracterDia().toUpperCase());

            resultados.add(toResponse(calendarioHibridoRepository.save(entity)));
        }

        return resultados;
    }

    @Override
    public void eliminarCalendario(UUID id) {
        UUID tenantId = requireTenant();
        CalendarioHibrido entity = calendarioHibridoRepository.findByIdAndEmpleado_EmpresaId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Calendario hibrido no encontrado para la empresa autenticada."));
        calendarioHibridoRepository.delete(entity);
    }

    private UUID requireTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no identificado en token JWT.");
        }
        return tenantId;
    }

    private Empleado requireEmpleadoTenant(UUID empleadoId, UUID tenantId) {
        return empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa autenticada."));
    }

    private void validarCaracterDia(String caracterDia) {
        if (caracterDia == null || caracterDia.isBlank()) {
            throw new BadRequestException("El caracter del dia es obligatorio.");
        }
        String value = caracterDia.toUpperCase();
        if (!PRESENCIAL.equals(value) && !REMOTO.equals(value)) {
            throw new BadRequestException("El caracter del dia debe ser PRESENCIAL o REMOTO.");
        }
    }

    private CalendarioHibridoResponse toResponse(CalendarioHibrido entity) {
        return new CalendarioHibridoResponse(
                entity.getId(),
                entity.getEmpleado().getId(),
                entity.getFecha(),
                entity.getCaracterDia()
        );
    }
}

