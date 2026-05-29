package com.proyecto.version1.Features.Empresas.dto;

/**
 * Generic API response wrapper used by service layer to include a message and data.
 */
public record ApiResponse<T>(boolean success, String message, T data) {}

