package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.LoginRequest;
import com.example.S_PACE.dto.request.SignUpRequest;
import com.example.S_PACE.dto.response.LoginResponse;
import com.example.S_PACE.dto.response.UserResponse;
import com.example.S_PACE.enums.ErrorStatus;
import com.example.S_PACE.enums.UserStatus;
import com.example.S_PACE.exception.AuthenticationException;
import com.example.S_PACE.mapper.UserMapper;
import com.example.S_PACE.pojo.Role;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.RoleRepository;
import com.example.S_PACE.repository.UserRepository;
import com.example.S_PACE.utils.JwtTokenProvider;
import com.example.S_PACE.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse register(SignUpRequest signUpRequest) {
        logger.info("Starting user registration for email: {}", signUpRequest.getEmail());
        
        // Validate input
        if (signUpRequest.getEmail() == null || signUpRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorStatus.NULL_VALUE.getDescription());
        }
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmail(signUpRequest.getEmail());
        if (existingUser.isPresent()) {
            logger.warn("User with email {} already exists", signUpRequest.getEmail());
            throw new IllegalArgumentException(ErrorStatus.USER_ALREADY_EXISTS.getDescription());
        }

        // Perform database operations in a separate transactional method
        return performUserRegistration(signUpRequest);
    }

    @Transactional
    private UserResponse performUserRegistration(SignUpRequest signUpRequest) {
        try {
            // Find COLLABORATOR role (or EMPLOYEE as fallback)
            Role userRole = roleRepository.findByRoleName("COLLABORATOR")
                    .orElse(roleRepository.findByRoleName("EMPLOYEE")
                            .orElseThrow(() -> new RuntimeException(ErrorStatus.ROLE_NOT_FOUND.getDescription())));

            logger.info("Found role: {} for user registration", userRole.getRoleName());

            // Create new user
            User user = new User();
            // userId will be generated automatically
            user.setFullName(signUpRequest.getFullName());
            user.setEmail(signUpRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setPhone(signUpRequest.getPhone());
            user.setAddress(signUpRequest.getAddress());
            user.setRole(userRole);
            // createdAt will be set by @PrePersist
            user.setStatus(UserStatus.ACTIVE); // Set user as active after registration

            // Save user (createdAt will be set by @PrePersist)
            User savedUser = userRepository.save(user);
            logger.info("User registered successfully with ID: {}", savedUser.getUserId());

            // Convert to response DTO
            return userMapper.toUserResponse(savedUser);
            
        } catch (Exception ex) {
            logger.error("Error during user registration: {}", ex.getMessage(), ex);
            throw new RuntimeException(ErrorStatus.REGISTRATION_FAILED.getDescription() + ": " + ex.getMessage());
        }
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("Starting login process for email: {}", loginRequest.getEmail());
        
        // Validate input
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorStatus.NULL_VALUE.getDescription());
        }
        
        try {
            // Find user by email
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new AuthenticationException(ErrorStatus.USER_NOT_FOUND.getDescription()));

            logger.info("User found: {} for login attempt", user.getEmail());

            // Check if user is active
            if (user.getStatus() != UserStatus.ACTIVE) {
                logger.warn("User {} is not active, status: {}", user.getEmail(), user.getStatus());
                throw new AuthenticationException(ErrorStatus.ACCOUNT_INACTIVE.getDescription() + ": " + user.getEmail());
            }

            // Verify password
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                logger.warn("Invalid password for user: {}", user.getEmail());
                throw new AuthenticationException(ErrorStatus.INVALID_CREDENTIALS.getDescription());
            }

            logger.info("Login successful for user: {}", user.getEmail());

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(user);

            // Create login response
            return LoginResponse.builder()
                    .token(token)
                    .user(userMapper.toUserResponse(user))
                    .tokenType("Bearer")
                    .build();
                    
        } catch (AuthenticationException ex) {
            logger.error("Authentication failed for user {}: {}", loginRequest.getEmail(), ex.getMessage());
            throw ex; // Re-throw authentication exceptions as-is
        } catch (Exception ex) {
            logger.error("Unexpected error during login: {}", ex.getMessage(), ex);
            throw new RuntimeException(ErrorStatus.INVALID_CREDENTIALS.getDescription() + ": " + ex.getMessage());
        }
    }
}
