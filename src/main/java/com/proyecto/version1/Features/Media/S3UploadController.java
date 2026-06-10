package com.proyecto.version1.Features.Media;

import com.proyecto.version1.Features.Media.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@PreAuthorize("hasAnyAuthority('EMPLEADO', 'ADMIN_RRHH', 'SUPERADMIN')")
@RequiredArgsConstructor
@Tag(name = "Carga de archivos a S3", description = "Endpoints para subir evidencias de justificaciones y fotos de empleados a Amazon S3")
public class S3UploadController {

    private final S3Service s3Service;

    @PostMapping("/upload/justificacion")
    @Operation(summary = "Subir comprobante de justificación", description = "Sube un archivo de soporte (evidencia) al bucket de S3 en la carpeta 'justificaciones'")
    public ResponseEntity<Map<String, String>> subirJustificacion(@RequestParam("file") MultipartFile file) {
        var result = s3Service.uploadFileWithKey(file, "justificaciones");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/upload/empleado")
    @Operation(summary = "Subir foto de empleado", description = "Sube la imagen de perfil/foto de un empleado al bucket de S3 en la carpeta 'empleados'")
    public ResponseEntity<Map<String, String>> subirFotoEmpleado(@RequestParam("file") MultipartFile file) {
        var result = s3Service.uploadFileWithKey(file, "empleados");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/download-url")
    @Operation(summary = "Obtener URL firmada para ver/descargar un archivo de S3",
               description = "Genera una URL temporal (30 min) para visualizar un archivo privado de S3. Enviar el 'fileKey' almacenado en BD.")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@RequestParam String fileKey) {
        String url = s3Service.generatePresignedFileDownloadUrl(fileKey);
        return ResponseEntity.ok(Map.of("downloadUrl", url));
    }

    @GetMapping("/presigned-url")
    @Operation(summary = "Obtener URL firmada para subir archivo directamente a S3",
               description = "Genera una URL temporal que permite al frontend subir un archivo a S3 sin pasar por el backend.")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @RequestParam String folder,
            @RequestParam String fileName,
            @RequestParam String contentType) {

        java.util.UUID tenantId = com.proyecto.version1.security.TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return ResponseEntity.status(401).build();
        }

        String url = s3Service.generatePresignedFileUploadUrl(tenantId, folder, fileName, contentType);
        String finalKey = String.format("tenants/%s/%s/%s", tenantId, folder, fileName);

        return ResponseEntity.ok(Map.of(
            "uploadUrl", url,
            "fileKey", finalKey
        ));
    }
}
