package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.LoginRequest;
import com.example.S_PACE.dto.request.SignUpRequest;
import com.example.S_PACE.dto.request.UserUpdateRequest;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    // Default avatar URL - configured in application.yml
    @Value("${app.default-avatar:/images/avatars/avatar.jpg}")
    private String defaultAvatarUrl;

    @Override
    @Transactional
    public UserResponse register(SignUpRequest signUpRequest) {
        logger.info("Starting user registration for email: {}", signUpRequest.getEmail());
        
        // Validate input
        if (signUpRequest.getEmail() == null || signUpRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorStatus.NULL_VALUE.getDescription());
        }
        
        // Validate password confirmation
        if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
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

            // Create new user with only essential fields
            User user = new User();
            // userId will be generated automatically
            user.setFullName(signUpRequest.getFullName());
            user.setEmail(signUpRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setRole(userRole);
            
            // Set default avatar for all new users
            user.setAvatar(defaultAvatarUrl);
            logger.info("Using default avatar for new user: {}", defaultAvatarUrl);
            
            // Set default values for optional fields (can be updated later in profile)
            user.setPhone(null); // Will be updated in profile
            user.setAddress(null); // Will be updated in profile
            user.setGender(null); // Will be updated in profile
            
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

    // ========== NEW METHODS FOR USER CONTROLLER ==========

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getStatus() != UserStatus.DELETED) // Exclude deleted users
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        logger.info("Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("User has been deleted");
        }
        
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest updateRequest) {
        logger.info("Updating user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("Cannot update deleted user");
        }
        
        // Check if email is being changed and if it already exists
        if (!user.getEmail().equals(updateRequest.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(updateRequest.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getUserId().equals(userId)) {
                throw new IllegalArgumentException("Email already exists");
            }
        }
        
        // Update user fields
        user.setFullName(updateRequest.getFullName());
        user.setEmail(updateRequest.getEmail());
        user.setPhone(updateRequest.getPhone());
        user.setAddress(updateRequest.getAddress());
        user.setGender(updateRequest.getGender());
        if (updateRequest.getAvatar() != null) {
            user.setAvatar(updateRequest.getAvatar());
        }
        
        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully with ID: {}", updatedUser.getUserId());
        
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        logger.info("Soft deleting user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("User is already deleted");
        }
        
        // Soft delete by changing status to DELETED
        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
        
        logger.info("User soft deleted successfully with ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByStatus(UserStatus status) {
        logger.info("Fetching users by status: {}", status);
        return userRepository.findByStatus(status)
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(String roleName) {
        logger.info("Fetching users by role: {}", roleName);
        return userRepository.findByRoleRoleName(roleName)
                .stream()
                .filter(user -> user.getStatus() != UserStatus.DELETED) // Exclude deleted users
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }
}
