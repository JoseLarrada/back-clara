package com.proyecto.version1.Features.Empleados.mapper;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoCreateRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoResponse;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoUpdateRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminEmpleadoMapper {

    public Empleado toEntity(AdminEmpleadoCreateRequest request, UUID empresaId, String encodedPassword) {
        return Empleado.builder()
                .empresaId(empresaId)
                .nombreCompleto(request.nombreCompleto())
                .email(request.email())
                .passwordHash(encodedPassword)
                .rol(request.rol() != null ? request.rol().toUpperCase() : "EMPLEADO")
                .modalidadPerfil(request.modalidadPerfil() != null ? request.modalidadPerfil().toUpperCase() : null)
                .fotoPatronUrl(request.fotoPatronUrl())
                .saldoVacaciones(request.saldoVacaciones())
                .activo(request.activo() != null ? request.activo() : true)
                .build();
    }

    public void applyUpdate(AdminEmpleadoUpdateRequest request, Empleado entity) {
        entity.setNombreCompleto(request.nombreCompleto());
        entity.setEmail(request.email());
        entity.setRol(request.rol() != null ? request.rol().toUpperCase() : null);
        entity.setModalidadPerfil(request.modalidadPerfil() != null ? request.modalidadPerfil().toUpperCase() : null);
        entity.setSaldoVacaciones(request.saldoVacaciones());
        entity.setActivo(request.activo());
        if (request.fotoPatronUrl() != null) {
            entity.setFotoPatronUrl(request.fotoPatronUrl());
        }
    }

    public AdminEmpleadoResponse toResponse(Empleado entity) {
        return new AdminEmpleadoResponse(
                entity.getId(),
                entity.getEmpresaId(),
                entity.getNombreCompleto(),
                entity.getEmail(),
                entity.getRol(),
                entity.getModalidadPerfil(),
                entity.getFotoPatronUrl(),
                entity.getSaldoVacaciones(),
                entity.getActivo(),
                entity.getCreadoEn()
        );
    }
}

