package com.proyecto.version1.Features.Media.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file, String folder);
}
