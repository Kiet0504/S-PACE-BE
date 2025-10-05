package com.example.S_PACE.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface FileUploadService {
    String uploadAvatar(MultipartFile file, UUID userId) throws IOException;
    String uploadCV(MultipartFile file, UUID userId) throws IOException;
    String uploadCertificate(MultipartFile file, UUID userId) throws IOException;
    void deleteAvatar(String filePath) throws IOException;
    void deleteFile(String fileUrl) throws IOException;
    boolean isValidImageFile(MultipartFile file);
    boolean isValidDocumentFile(MultipartFile file);
} 