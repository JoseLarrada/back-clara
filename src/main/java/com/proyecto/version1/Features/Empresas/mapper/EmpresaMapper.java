package com.proyecto.version1.Features.Empresas.mapper;

import com.proyecto.version1.Features.Empresas.Empresa;
import com.proyecto.version1.Features.Empresas.dto.EmpresaCreateRequest;
import com.proyecto.version1.Features.Empresas.dto.EmpresaResponse;
import com.proyecto.version1.Features.Empresas.dto.EmpresaUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmpresaMapper {
    Empresa toEntity(EmpresaCreateRequest request);
    EmpresaResponse toResponse(Empresa entity);
    List<EmpresaResponse> toResponseList(List<Empresa> entidades);
    void updateFromUpdateRequest(EmpresaUpdateRequest request, @MappingTarget Empresa entity);
}

