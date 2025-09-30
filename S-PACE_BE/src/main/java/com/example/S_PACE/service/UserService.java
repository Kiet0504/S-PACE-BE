package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.LoginRequest;
import com.example.S_PACE.dto.request.SignUpRequest;
import com.example.S_PACE.dto.request.UserUpdateRequest;
import com.example.S_PACE.dto.response.LoginResponse;
import com.example.S_PACE.dto.response.UserResponse;
import com.example.S_PACE.enums.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse register(SignUpRequest signUpRequest);
    LoginResponse login(LoginRequest loginRequest);
    
    // New methods for UserController
    List<UserResponse> getAllUsers();
    UserResponse getUserById(UUID userId);
    UserResponse updateUser(UUID userId, UserUpdateRequest updateRequest);
    void deleteUser(UUID userId);
    List<UserResponse> getUsersByStatus(UserStatus status);
    List<UserResponse> getUsersByRole(String roleName);
}
