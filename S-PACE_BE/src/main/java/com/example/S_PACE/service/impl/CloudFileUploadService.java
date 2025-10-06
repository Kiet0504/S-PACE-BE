package com.example.S_PACE.service.impl;

import com.example.S_PACE.service.CloudStorageService;
import com.example.S_PACE.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "cloud")
public class CloudFileUploadService implements FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(CloudFileUploadService.class);

    @Autowired
    private CloudStorageService cloudStorageService;

    @Value("${app.storage.type:cloud}")
    private String storageType;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
        "application/pdf", 
        "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Override
    public String uploadAvatar(MultipartFile file, UUID userId) throws IOException {
        logger.info("Uploading avatar for user: {} using {} storage", userId, storageType);
        
        if (!isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid image file type. Only JPEG, PNG, GIF, and WebP are allowed");
        }

        return cloudStorageService.uploadAvatar(file, userId);
    }

    @Override
    public String uploadCV(MultipartFile file, UUID userId) throws IOException {
        logger.info("Uploading CV for user: {} using {} storage", userId, storageType);
        
        if (!isValidDocumentFile(file)) {
            throw new IllegalArgumentException("Invalid document file type. Only PDF and Word documents are allowed");
        }

        return cloudStorageService.uploadCV(file, userId);
    }

    @Override
    public String uploadCertificate(MultipartFile file, UUID userId) throws IOException {
        logger.info("Uploading certificate for user: {} using {} storage", userId, storageType);
        
        // Allow both image and document files for certificates
        if (!isValidImageFile(file) && !isValidDocumentFile(file)) {
            throw new IllegalArgumentException("Invalid certificate file type. Only images (JPEG, PNG, GIF, WebP) and documents (PDF, Word) are allowed");
        }

        return cloudStorageService.uploadCertificate(file, userId);
    }

    @Override
    public void deleteAvatar(String filePath) throws IOException {
        logger.info("Deleting avatar: {}", filePath);
        cloudStorageService.deleteFile(filePath);
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {
        logger.info("Deleting file: {}", fileUrl);
        cloudStorageService.deleteFile(fileUrl);
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    @Override
    public boolean isValidDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null && ALLOWED_DOCUMENT_TYPES.contains(contentType.toLowerCase());
    }
}
