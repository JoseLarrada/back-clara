package com.proyecto.version1.Features.Empresas.dto;

import java.util.List;

/**
 * Generic paginated response wrapper for API responses.
 * @param <T> the type of content in the page
 */
public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {}

