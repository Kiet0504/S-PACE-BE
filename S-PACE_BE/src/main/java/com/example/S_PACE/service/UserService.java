package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.LoginRequest;
import com.example.S_PACE.dto.request.SignUpRequest;
import com.example.S_PACE.dto.response.LoginResponse;
import com.example.S_PACE.dto.response.UserResponse;

public interface UserService {
    UserResponse register(SignUpRequest signUpRequest);
    LoginResponse login(LoginRequest loginRequest);
}
