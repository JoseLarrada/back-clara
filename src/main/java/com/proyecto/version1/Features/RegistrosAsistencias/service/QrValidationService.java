package com.proyecto.version1.Features.RegistrosAsistencias.service;

import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class QrValidationService {

    private static final long QR_EXPIRATION_LIMIT_SECONDS = 5L;

    /**
     * Valida un token QR dinámico contra las restricciones de tenant y expiración.
     * Formato del token esperado: QR_{empresaId}_{timestampSeconds}
     *
     * @param tokenQr  El token QR recibido
     * @param tenantId El tenant ID autenticado
     */
    public void validarQrDinamico(String tokenQr, UUID tenantId) {
        if (tokenQr == null || !tokenQr.startsWith("QR_")) {
            throw new BadRequestException("Formato de código QR dinámico inválido.");
        }

        String[] parts = tokenQr.split("_");
        if (parts.length < 3) {
            throw new BadRequestException("Contenido de código QR dinámico incompleto o adulterado.");
        }

        UUID tokenEmpresaId;
        try {
            tokenEmpresaId = UUID.fromString(parts[1]);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("El identificador de empresa en el código QR no es válido.");
        }

        if (!tenantId.equals(tokenEmpresaId)) {
            throw new BadRequestException("El código QR escaneado no corresponde a su empresa registrada.");
        }

        long tokenTimestampSeconds;
        try {
            tokenTimestampSeconds = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            throw new BadRequestException("La marca de tiempo del código QR no es válida.");
        }

        long currentSeconds = Instant.now().getEpochSecond();
        long diffSeconds = currentSeconds - tokenTimestampSeconds;

        // Si el código QR es de hace más de 5 segundos, se rechaza para evitar fraudes por compartir capturas
        if (diffSeconds > QR_EXPIRATION_LIMIT_SECONDS) {
            throw new BadRequestException("El código QR ha expirado. Por favor, escanee el código actualizado.");
        }

        // Validación de reloj del servidor (no permitir marcas en el futuro con más de 2 segundos de desfase)
        if (diffSeconds < -2) {
            throw new BadRequestException("Falla de sincronización de hora en el código QR.");
        }
    }
}
