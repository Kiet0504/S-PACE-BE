package com.example.S_PACE.service.impl;

import com.example.S_PACE.service.CloudStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "cloud")
public class AwsS3CloudStorageService implements CloudStorageService {

    private static final Logger logger = LoggerFactory.getLogger(AwsS3CloudStorageService.class);

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.cloudfront-domain:}")
    private String cloudfrontDomain;

    @Value("${aws.s3.public-url:}")
    private String publicUrl;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
        "application/pdf", "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public String uploadAvatar(MultipartFile file, UUID userId) throws IOException {
        logger.info("Uploading avatar to S3 for user: {}", userId);

        validateFile(file, ALLOWED_IMAGE_TYPES, "avatar");

        String key = generateFileKey("avatars", userId, file.getOriginalFilename());
        
        return uploadToS3(file, key, "public-read");
    }

    @Override
    public String uploadCV(MultipartFile file, UUID userId) throws IOException {
        logger.info("Uploading CV to S3 for user: {}", userId);

        validateFile(file, ALLOWED_DOCUMENT_TYPES, "CV");

        String key = generateFileKey("cvs", userId, file.getOriginalFilename());
        
        return uploadToS3(file, key, "private");
    }

    @Override
    public String uploadCertificate(MultipartFile file, UUID userId) throws IOException {
        logger.info("Uploading certificate to S3 for user: {}", userId);

        // Allow both image and document types for certificates
        List<String> allowedTypes = new ArrayList<>(ALLOWED_IMAGE_TYPES);
        allowedTypes.addAll(ALLOWED_DOCUMENT_TYPES);
        validateFile(file, allowedTypes, "certificate");

        String key = generateFileKey("certificates", userId, file.getOriginalFilename());
        
        return uploadToS3(file, key, "private");
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {
        try {
            String key = extractKeyFromUrl(fileUrl);
            logger.info("Deleting file from S3: {}", key);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            logger.info("File deleted successfully from S3: {}", key);

        } catch (Exception e) {
            logger.error("Failed to delete file from S3: {}", e.getMessage(), e);
            throw new IOException("Failed to delete file from S3", e);
        }
    }

    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.error("Error checking if file exists: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public long getFileSize(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headRequest);
            return response.contentLength();

        } catch (NoSuchKeyException e) {
            return -1;
        } catch (Exception e) {
            logger.error("Error getting file size: {}", e.getMessage(), e);
            return -1;
        }
    }

    private String uploadToS3(MultipartFile file, String key, String acl) throws IOException {
        try {
            // Set content type
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Build put object request without ACL (bucket policy handles permissions)
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();

            // Upload file
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Generate public URL
            String publicUrl = generatePublicUrl(key);
            logger.info("File uploaded successfully to S3: {}", publicUrl);

            return publicUrl;

        } catch (Exception e) {
            logger.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    private String generateFileKey(String folder, UUID userId, String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileExtension = getFileExtension(originalFilename);
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s/%s/%s_%s_%s%s", 
                folder, timestamp, userId, uniqueId, System.currentTimeMillis(), fileExtension);
    }

    private String generatePublicUrl(String key) {
        if (!cloudfrontDomain.isEmpty()) {
            // Use CloudFront URL if configured
            return String.format("https://%s/%s", cloudfrontDomain, key);
        } else if (!publicUrl.isEmpty()) {
            // Use custom public URL if configured
            return String.format("%s/%s", publicUrl, key);
        } else {
            // Use default S3 public URL
            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
        }
    }

    private String extractKeyFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            
            // Remove leading slash
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            
            return path;
        } catch (Exception e) {
            logger.error("Failed to extract key from URL: {}", fileUrl, e);
            throw new IllegalArgumentException("Invalid file URL: " + fileUrl);
        }
    }

    private void validateFile(MultipartFile file, List<String> allowedTypes, String fileType) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size too large. Maximum size is %dMB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                String.format("Invalid %s file type. Allowed types: %s", fileType, allowedTypes)
            );
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}

