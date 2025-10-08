package com.example.S_PACE.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface CloudStorageService {
    
    /**
     * Upload avatar to cloud storage
     * @param file The avatar file to upload
     * @param userId The user ID
     * @return The public URL of the uploaded file
     * @throws IOException if upload fails
     */
    String uploadAvatar(MultipartFile file, UUID userId) throws IOException;
    
    /**
     * Upload CV to cloud storage
     * @param file The CV file to upload
     * @param userId The user ID
     * @return The public URL of the uploaded file
     * @throws IOException if upload fails
     */
    String uploadCV(MultipartFile file, UUID userId) throws IOException;
    
    /**
     * Upload certificate to cloud storage
     * @param file The certificate file to upload
     * @param userId The user ID
     * @return The public URL of the uploaded file
     * @throws IOException if upload fails
     */
    String uploadCertificate(MultipartFile file, UUID userId) throws IOException;
    
    /**
     * Upload event image to cloud storage
     * @param file The event image file to upload
     * @return The public URL of the uploaded file
     * @throws IOException if upload fails
     */
    String uploadEventImage(MultipartFile file) throws IOException;
    
    /**
     * Delete file from cloud storage
     * @param fileUrl The public URL of the file to delete
     * @throws IOException if deletion fails
     */
    void deleteFile(String fileUrl) throws IOException;
    
    /**
     * Check if file exists in cloud storage
     * @param fileUrl The public URL of the file
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String fileUrl);
    
    /**
     * Get file size from cloud storage
     * @param fileUrl The public URL of the file
     * @return File size in bytes, or -1 if file doesn't exist
     */
    long getFileSize(String fileUrl);
}

