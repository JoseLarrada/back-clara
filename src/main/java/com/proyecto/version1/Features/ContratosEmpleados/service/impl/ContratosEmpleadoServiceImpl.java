package com.proyecto.version1.Features.ContratosEmpleados.service.impl;

import com.proyecto.version1.Features.Auditoria.AuditoriaService;
import com.proyecto.version1.Features.ContratosEmpleados.ContratosEmpleado;
import com.proyecto.version1.Features.ContratosEmpleados.ContratosEmpleadoRepository;
import com.proyecto.version1.Features.ContratosEmpleados.dto.*;
import com.proyecto.version1.Features.ContratosEmpleados.mapper.ContratoMapper;
import com.proyecto.version1.Features.ContratosEmpleados.service.ContratosEmpleadoService;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContratosEmpleadoServiceImpl implements ContratosEmpleadoService {

    private final ContratosEmpleadoRepository contratosEmpleadoRepository;
    private final EmpleadosRepository empleadosRepository;
    private final AuditoriaService auditoriaService;

    @Override
    public ContratoResponse crearContrato(ContratoCreateRequest request) {
        UUID tenantId = requireTenant();
        Empleado empleado = obtenerEmpleadoDelTenant(request.empleadoId(), tenantId);

        if (request.activo() && contratosEmpleadoRepository.existsByEmpleado_IdAndActivoTrue(request.empleadoId())) {
            throw new BadRequestException("El empleado ya tiene un contrato activo. Cierre el contrato anterior antes de registrar uno nuevo.");
        }

        if (request.fechaRetiro() != null && request.fechaRetiro().isBefore(request.fechaIngreso())) {
            throw new BadRequestException("La fecha de retiro no puede ser anterior a la fecha de ingreso.");
        }

        ContratosEmpleado contrato = ContratosEmpleado.builder()
                .empleado(empleado)
                .empresaId(tenantId)
                .salarioBaseMensual(request.salarioBaseMensual())
                .tipoMoneda(request.tipoMoneda())
                .tipoContrato(request.tipoContrato())
                .fechaIngreso(request.fechaIngreso())
                .fechaRetiro(request.fechaRetiro())
                .activo(request.activo())
                .build();

        ContratosEmpleado saved = contratosEmpleadoRepository.save(contrato);

        auditoriaService.registrarLog("CREAR_CONTRATO_EMPLEADO", "contratos_empleados", saved.getId(), null, saved);

        return ContratoMapper.toResponse(saved);
    }

    @Override
    public ContratoResponse actualizarContrato(UUID id, ContratoUpdateRequest request) {
        UUID tenantId = requireTenant();
        ContratosEmpleado contrato = buscarContrato(id, tenantId);

        if (request.fechaRetiro() != null && request.fechaRetiro().isBefore(request.fechaIngreso())) {
            throw new BadRequestException("La fecha de retiro no puede ser anterior a la fecha de ingreso.");
        }

        // Si cambia el estado a activo, validar que no haya otro activo
        if (request.activo() && !contrato.getActivo() && 
                contratosEmpleadoRepository.existsByEmpleado_IdAndActivoTrue(contrato.getEmpleado().getId())) {
            throw new BadRequestException("El empleado ya tiene otro contrato activo. Cárguelo como inactivo o desactive el anterior.");
        }

        ContratosEmpleado anterior = ContratosEmpleado.builder()
                .id(contrato.getId())
                .empresaId(contrato.getEmpresaId())
                .empleado(contrato.getEmpleado())
                .salarioBaseMensual(contrato.getSalarioBaseMensual())
                .tipoMoneda(contrato.getTipoMoneda())
                .tipoContrato(contrato.getTipoContrato())
                .fechaIngreso(contrato.getFechaIngreso())
                .fechaRetiro(contrato.getFechaRetiro())
                .activo(contrato.getActivo())
                .build();

        contrato.setSalarioBaseMensual(request.salarioBaseMensual());
        contrato.setTipoMoneda(request.tipoMoneda());
        contrato.setTipoContrato(request.tipoContrato());
        contrato.setFechaIngreso(request.fechaIngreso());
        contrato.setFechaRetiro(request.fechaRetiro());
        contrato.setActivo(request.activo());

        ContratosEmpleado updated = contratosEmpleadoRepository.save(contrato);

        auditoriaService.registrarLog("ACTUALIZAR_CONTRATO_EMPLEADO", "contratos_empleados", updated.getId(), anterior, updated);

        return ContratoMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ContratoResponse obtenerContrato(UUID id) {
        UUID tenantId = requireTenant();
        return ContratoMapper.toResponse(buscarContrato(id, tenantId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratoResponse> listarPorEmpleado(UUID empleadoId) {
        UUID tenantId = requireTenant();
        // Verificar que el empleado existe en la empresa actual
        obtenerEmpleadoDelTenant(empleadoId, tenantId);

        return contratosEmpleadoRepository.findByEmpleado_IdOrderByFechaIngresoDesc(empleadoId)
                .stream()
                .map(ContratoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ContratoResponse> listarContratos(Pageable pageable) {
        // El RLS y el filtrado por TenantId de Hibernate garantizan que el findAll esté aislado
        Page<ContratosEmpleado> page = contratosEmpleadoRepository.findAll(pageable);
        return new PageResponse<>(
                page.map(ContratoMapper::toResponse).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public ContratoResponse cerrarContrato(UUID id, ContratoCloseRequest request) {
        UUID tenantId = requireTenant();
        ContratosEmpleado contrato = buscarContrato(id, tenantId);

        if (!contrato.getActivo()) {
            throw new BadRequestException("El contrato no está activo.");
        }

        if (request.fechaRetiro().isBefore(contrato.getFechaIngreso())) {
            throw new BadRequestException("La fecha de terminación no puede ser anterior a la fecha de inicio del contrato.");
        }

        ContratosEmpleado anterior = ContratosEmpleado.builder()
                .id(contrato.getId())
                .empresaId(contrato.getEmpresaId())
                .empleado(contrato.getEmpleado())
                .salarioBaseMensual(contrato.getSalarioBaseMensual())
                .tipoMoneda(contrato.getTipoMoneda())
                .tipoContrato(contrato.getTipoContrato())
                .fechaIngreso(contrato.getFechaIngreso())
                .fechaRetiro(contrato.getFechaRetiro())
                .activo(contrato.getActivo())
                .build();

        contrato.setActivo(false);
        contrato.setFechaRetiro(request.fechaRetiro());

        ContratosEmpleado closed = contratosEmpleadoRepository.save(contrato);

        auditoriaService.registrarLog("CERRAR_CONTRATO_EMPLEADO", "contratos_empleados", closed.getId(), anterior, closed);

        return ContratoMapper.toResponse(closed);
    }

    @Override
    public ContratoResponse extenderContrato(UUID id, ContratoExtendRequest request) {
        UUID tenantId = requireTenant();
        ContratosEmpleado contrato = buscarContrato(id, tenantId);

        if (!contrato.getActivo()) {
            throw new BadRequestException("El contrato no está activo y no se puede extender.");
        }

        if (request.nuevaFechaRetiro().isBefore(contrato.getFechaIngreso())) {
            throw new BadRequestException("La nueva fecha de vencimiento no puede ser anterior a la fecha de ingreso.");
        }

        if (contrato.getFechaRetiro() != null && request.nuevaFechaRetiro().isBefore(contrato.getFechaRetiro())) {
            throw new BadRequestException("La nueva fecha de vencimiento debe ser posterior a la fecha de vencimiento actual.");
        }

        ContratosEmpleado anterior = ContratosEmpleado.builder()
                .id(contrato.getId())
                .empresaId(contrato.getEmpresaId())
                .empleado(contrato.getEmpleado())
                .salarioBaseMensual(contrato.getSalarioBaseMensual())
                .tipoMoneda(contrato.getTipoMoneda())
                .tipoContrato(contrato.getTipoContrato())
                .fechaIngreso(contrato.getFechaIngreso())
                .fechaRetiro(contrato.getFechaRetiro())
                .activo(contrato.getActivo())
                .build();

        contrato.setFechaRetiro(request.nuevaFechaRetiro());
        if (request.nuevoSalarioBase() != null) {
            contrato.setSalarioBaseMensual(request.nuevoSalarioBase());
        }

        ContratosEmpleado extended = contratosEmpleadoRepository.save(contrato);

        auditoriaService.registrarLog("EXTENDER_CONTRATO_EMPLEADO", "contratos_empleados", extended.getId(), anterior, extended);

        return ContratoMapper.toResponse(extended);
    }

    @Override
    public void eliminarContrato(UUID id) {
        UUID tenantId = requireTenant();
        ContratosEmpleado contrato = buscarContrato(id, tenantId);

        contratosEmpleadoRepository.delete(contrato);
        auditoriaService.registrarLog("ELIMINAR_CONTRATO_EMPLEADO", "contratos_empleados", id, contrato, null);
    }

    private ContratosEmpleado buscarContrato(UUID id, UUID tenantId) {
        // Al usar findById, Hibernate validará automáticamente por TenantId si está configurada la sesión
        // Pero para asegurar consistencia multi-tenant explícita:
        ContratosEmpleado contrato = contratosEmpleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato de empleado no encontrado con ID: " + id));

        if (!tenantId.equals(contrato.getEmpresaId())) {
            throw new ResourceNotFoundException("Contrato de empleado no encontrado para la empresa autenticada.");
        }
        return contrato;
    }

    private Empleado obtenerEmpleadoDelTenant(UUID empleadoId, UUID tenantId) {
        return empleadosRepository.findByIdAndEmpresaId(empleadoId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado para la empresa autenticada."));
    }

    private UUID requireTenant() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no identificado en token JWT.");
        }
        return tenantId;
    }
}
