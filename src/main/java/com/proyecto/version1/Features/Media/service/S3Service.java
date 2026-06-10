package com.proyecto.version1.Features.Media.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.UUID;

public interface S3Service {
    String uploadFile(MultipartFile file, String folder);
    Map<String, String> uploadFileWithKey(MultipartFile file, String folder);
    String generatePresignedFileUploadUrl(UUID tenantId, String folder, String fileName, String contentType);
    String generatePresignedFileDownloadUrl(String fileKey);
}

