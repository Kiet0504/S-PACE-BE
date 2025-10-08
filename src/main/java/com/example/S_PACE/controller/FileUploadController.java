package com.example.S_PACE.controller;

import com.example.S_PACE.dto.response.FileUploadResponse;
import com.example.S_PACE.dto.response.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File Upload", description = "File upload management APIs")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    @Operation(summary = "Upload file", description = "Upload an image file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or file too large"),
            @ApiResponse(responseCode = "500", description = "Upload failed")
    })
    public ResponseEntity<ResponseDTO<FileUploadResponse>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file is not empty
            if (file.isEmpty()) {
                logger.warn("Upload failed: File is empty");
                return ResponseEntity.badRequest()
                        .body(new ResponseDTO<>(false, "File is empty", null));
            }

            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE) {
                logger.warn("Upload failed: File size exceeds limit. Size: {} bytes", file.getSize());
                return ResponseEntity.badRequest()
                        .body(new ResponseDTO<>(false, "File size exceeds 10MB limit", null));
            }

            // Validate file type (only images)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.warn("Upload failed: Invalid file type: {}", contentType);
                return ResponseEntity.badRequest()
                        .body(new ResponseDTO<>(false, "Only image files are allowed", null));
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                logger.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File uploaded successfully: {}", uniqueFilename);

            // Return response
            FileUploadResponse fileResponse = new FileUploadResponse("/uploads/" + uniqueFilename);
            return ResponseEntity.ok(new ResponseDTO<>(true, "File uploaded successfully", fileResponse));

        } catch (IOException e) {
            logger.error("File upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "File upload failed: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Unexpected error during file upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Upload failed: " + e.getMessage(), null));
        }
    }
}