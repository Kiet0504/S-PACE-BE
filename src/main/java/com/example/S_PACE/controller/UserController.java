package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.AdminCreateUserRequest;
import com.example.S_PACE.dto.request.UserUpdateRequest;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.dto.response.UserResponse;
import com.example.S_PACE.enums.UserStatus;
import com.example.S_PACE.service.FileUploadService;
import com.example.S_PACE.service.UserService;
import com.example.S_PACE.utils.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User management APIs for administrators")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/admin/create")
    @Operation(summary = "Create user by admin", description = "Admin endpoint to create user accounts with specified roles and assignments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or user already exists"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<UserResponse>> createUserByAdmin(
            @Valid @RequestBody AdminCreateUserRequest createRequest) {
        try {
            logger.info("Admin creating user with email: {} and role: {}", createRequest.getEmail(), createRequest.getRoleName());
            UserResponse createdUser = userService.createUserByAdmin(createRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "User created successfully", createdUser));
        } catch (IllegalArgumentException ex) {
            logger.warn("User creation validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error creating user by admin: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to create user", null));
        }
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all active users (excludes deleted users)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ResponseDTO<List<UserResponse>>> getAllUsers() {
        try {
            logger.info("Fetching all users");
            List<UserResponse> users = userService.getAllUsers();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Users retrieved successfully", users));
        } catch (Exception ex) {
            logger.error("Error fetching all users: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch users", null));
        }
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ResponseDTO<UserResponse>> getUserById(@PathVariable UUID userId) {
        try {
            logger.info("Fetching user by ID: {}", userId);
            UserResponse user = userService.getUserById(userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "User retrieved successfully", user));
        } catch (IllegalArgumentException ex) {
            logger.warn("User not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch user", null));
        }
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ResponseDTO<UserResponse>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest updateRequest) {
        try {
            logger.info("Updating user with ID: {}", userId);
            UserResponse updatedUser = userService.updateUser(userId, updateRequest);
            return ResponseEntity.ok(new ResponseDTO<>(true, "User updated successfully", updatedUser));
        } catch (IllegalArgumentException ex) {
            logger.warn("User update validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error updating user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to update user", null));
        }
    }

    @PostMapping("/{userId}/avatar")
    @Operation(summary = "Upload user avatar", description = "Upload avatar image for user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or user not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<ResponseDTO<String>> uploadAvatar(
            @PathVariable UUID userId,
            @RequestParam("avatar") MultipartFile avatarFile,
            HttpServletRequest request) {
        try {
            logger.info("Uploading avatar for user: {}", userId);
            
            // Verify user exists and has permission
            UserResponse user = userService.getUserById(userId);
            
            // Upload the file
            String avatarPath = fileUploadService.uploadAvatar(avatarFile, userId);
            
            // Update user's avatar in database
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setFullName(user.getFullName());
            updateRequest.setEmail(user.getEmail());
            updateRequest.setPhone(user.getPhone());
            updateRequest.setAddress(user.getAddress());
            updateRequest.setGender(user.getGender());
            updateRequest.setAvatar(avatarPath);
            
            userService.updateUser(userId, updateRequest);
            
            logger.info("Avatar uploaded and updated successfully for user: {}", userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Avatar uploaded successfully", avatarPath));
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Avatar upload validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (IOException ex) {
            logger.error("File upload error for user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload avatar", null));
        } catch (Exception ex) {
            logger.error("Error uploading avatar for user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload avatar", null));
        }
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Soft delete user by changing status to DELETED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> deleteUser(@PathVariable UUID userId) {
        try {
            logger.info("Deleting user with ID: {}", userId);
            userService.deleteUser(userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "User deleted successfully", null));
        } catch (IllegalArgumentException ex) {
            logger.warn("User deletion validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error deleting user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to delete user", null));
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get users by status", description = "Retrieve users with a specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ResponseDTO<List<UserResponse>>> getUsersByStatus(@PathVariable UserStatus status) {
        try {
            logger.info("Fetching users by status: {}", status);
            List<UserResponse> users = userService.getUsersByStatus(status);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Users retrieved successfully", users));
        } catch (Exception ex) {
            logger.error("Error fetching users by status {}: {}", status, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch users", null));
        }
    }

    @GetMapping("/role/{roleName}")
    @Operation(summary = "Get users by role", description = "Retrieve users with a specific role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ResponseDTO<List<UserResponse>>> getUsersByRole(@PathVariable String roleName) {
        try {
            logger.info("Fetching users by role: {}", roleName);
            List<UserResponse> users = userService.getUsersByRole(roleName);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Users retrieved successfully", users));
        } catch (Exception ex) {
            logger.error("Error fetching users by role {}: {}", roleName, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch users", null));
        }
    }

    @GetMapping("/my-profile")
    @Operation(summary = "Get current user profile", description = "Retrieve the current authenticated user's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER', 'TEAM_LEADER', 'EMPLOYEE', 'COLLABORATOR')")
    public ResponseEntity<ResponseDTO<UserResponse>> getMyProfile(HttpServletRequest request) {
        try {
            UUID userId = getUserIdFromToken(request);
            logger.info("Fetching current user profile for user: {}", userId);
            UserResponse user = userService.getUserById(userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Profile retrieved successfully", user));
        } catch (IllegalArgumentException ex) {
            logger.warn("Profile not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching current user profile: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch profile", null));
        }
    }

    @PostMapping("/my-profile/avatar")
    @Operation(summary = "Upload current user's avatar", description = "Upload avatar image for the current authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER', 'TEAM_LEADER', 'EMPLOYEE', 'COLLABORATOR')")
    public ResponseEntity<ResponseDTO<String>> uploadMyAvatar(
            @RequestParam("avatar") MultipartFile avatarFile,
            HttpServletRequest request) {
        try {
            UUID userId = getUserIdFromToken(request);
            logger.info("User {} uploading their avatar", userId);
            
            // Get user info
            UserResponse user = userService.getUserById(userId);
            
            // Delete old avatar if exists
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                try {
                    fileUploadService.deleteAvatar(user.getAvatar());
                } catch (Exception e) {
                    logger.warn("Failed to delete old avatar: {}", e.getMessage());
                }
            }
            
            // Upload the new avatar
            String avatarPath = fileUploadService.uploadAvatar(avatarFile, userId);
            
            // Update user's avatar in database
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setFullName(user.getFullName());
            updateRequest.setEmail(user.getEmail());
            updateRequest.setPhone(user.getPhone());
            updateRequest.setAddress(user.getAddress());
            updateRequest.setGender(user.getGender());
            updateRequest.setAvatar(avatarPath);
            
            userService.updateUser(userId, updateRequest);
            
            logger.info("Avatar uploaded and updated successfully for user: {}", userId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Avatar uploaded successfully", avatarPath));
            
        } catch (IllegalArgumentException ex) {
            logger.warn("Avatar upload validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (IOException ex) {
            logger.error("File upload error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload avatar", null));
        } catch (Exception ex) {
            logger.error("Error uploading avatar: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to upload avatar", null));
        }
    }

    private UUID getUserIdFromToken(HttpServletRequest request) {
        String token = getJwtFromRequest(request);
        if (StringUtils.hasText(token)) {
            String userIdStr = jwtTokenProvider.getUserIdFromJWT(token);
            return UUID.fromString(userIdStr);
        }
        throw new IllegalArgumentException("Invalid or missing token");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}