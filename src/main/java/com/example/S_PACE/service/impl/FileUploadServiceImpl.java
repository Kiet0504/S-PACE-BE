package com.example.S_PACE.service.impl;

import com.example.S_PACE.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    @Value("${file.upload.avatar-dir:./uploads/avatars}")
    private String avatarUploadDir;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public String uploadAvatar(MultipartFile file, UUID userId) throws IOException {
        logger.info("Uploading avatar for user: {}", userId);

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!isValidImageFile(file)) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, GIF, and WebP are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size too large. Maximum size is 5MB");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(avatarUploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Created avatar upload directory: {}", uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = userId + "_" + System.currentTimeMillis() + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for database storage
        String relativePath = "/uploads/avatars/" + newFilename;
        logger.info("Avatar uploaded successfully: {}", relativePath);
        
        return relativePath;
    }

    @Override
    public void deleteAvatar(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }

        // Only delete files in uploads directory for security
        if (!filePath.startsWith("/uploads/avatars/")) {
            logger.warn("Attempted to delete file outside uploads directory: {}", filePath);
            return;
        }

        Path path = Paths.get("." + filePath); // Remove leading slash
        if (Files.exists(path)) {
            Files.delete(path);
            logger.info("Deleted avatar file: {}", filePath);
        }
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
} 