package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.AdminCreateUserRequest;
import com.example.S_PACE.dto.request.LoginRequest;
import com.example.S_PACE.dto.request.SignUpRequest;
import com.example.S_PACE.dto.request.UserUpdateRequest;
import com.example.S_PACE.dto.response.LoginResponse;
import com.example.S_PACE.dto.response.UserResponse;
import com.example.S_PACE.enums.ErrorStatus;
import com.example.S_PACE.enums.UserStatus;
import com.example.S_PACE.exception.AuthenticationException;
import com.example.S_PACE.mapper.UserMapper;
import com.example.S_PACE.pojo.Company;
import com.example.S_PACE.pojo.Role;
import com.example.S_PACE.pojo.Team;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.CompanyRepository;
import com.example.S_PACE.repository.RoleRepository;
import com.example.S_PACE.repository.TeamRepository;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
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
    private TeamRepository teamRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserMapper userMapper;

    // Avatar will be handled by frontend - no default avatar set in backend

    // Google OAuth configuration
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

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
        if (isEmailExists(signUpRequest.getEmail())) {
            logger.warn("User with email {} already exists", signUpRequest.getEmail());
            throw new IllegalArgumentException(ErrorStatus.USER_ALREADY_EXISTS.getDescription());
        }

        // Perform database operations in a separate transactional method
        return performUserRegistration(signUpRequest);
    }

    @Transactional
    private UserResponse performUserRegistration(SignUpRequest signUpRequest) {
        try {
            // Determine desired role from request; allow only EVENT_MANAGER or COLLABORATOR
            String requestedRoleName = signUpRequest.getRoleName();
            String effectiveRoleName;
            if (requestedRoleName == null || requestedRoleName.trim().isEmpty()) {
                effectiveRoleName = "COLLABORATOR"; // default
            } else {
                String upper = requestedRoleName.trim().toUpperCase();
                if (!upper.equals("EVENT_MANAGER") && !upper.equals("COLLABORATOR")) {
                    throw new IllegalArgumentException("Invalid role. Allowed: EVENT_MANAGER or COLLABORATOR");
                }
                effectiveRoleName = upper;
            }

            // Find the role in DB
            Role userRole = roleRepository.findByRoleName(effectiveRoleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + effectiveRoleName));

            logger.info("Found role: {} for user registration", userRole.getRoleName());

            // Create new user with only essential fields
            User user = new User();
            // userId will be generated automatically
            user.setFullName(signUpRequest.getFullName());
            user.setEmail(signUpRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setRole(userRole);
            
            // Avatar will be handled by frontend - no default avatar set
            user.setAvatar(null);
            logger.info("New user created without default avatar - frontend will handle default display");
            
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

    @Override
    @Transactional
    public UserResponse createUserByAdmin(AdminCreateUserRequest createRequest) {
        logger.info("Admin creating new user with email: {} and role: {}", createRequest.getEmail(), createRequest.getRoleName());

        // Validate input
        if (createRequest.getEmail() == null || createRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorStatus.NULL_VALUE.getDescription());
        }
        if (createRequest.getRoleName() == null || createRequest.getRoleName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name is required");
        }

        // Check if user already exists
        if (isEmailExists(createRequest.getEmail())) {
            logger.warn("User with email {} already exists", createRequest.getEmail());
            throw new IllegalArgumentException(ErrorStatus.USER_ALREADY_EXISTS.getDescription());
        }

        try {
            // Find role by name
            Role userRole = roleRepository.findByRoleName(createRequest.getRoleName())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + createRequest.getRoleName()));

            logger.info("Found role: {} for admin user creation", userRole.getRoleName());

            // Create new user
            User user = new User();
            user.setFullName(createRequest.getFullName());
            user.setEmail(createRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(createRequest.getPassword()));
            user.setRole(userRole);
            user.setPhone(createRequest.getPhone());
            user.setAddress(createRequest.getAddress());
            user.setGender(createRequest.getGender());

            // Set status or default to ACTIVE if null
            if (createRequest.getStatus() != null) {
                user.setStatus(createRequest.getStatus());
            } else {
                user.setStatus(UserStatus.ACTIVE);
            }

            // Set avatar if provided, otherwise leave null for frontend to handle
            if (createRequest.getAvatar() != null && !createRequest.getAvatar().trim().isEmpty()) {
                user.setAvatar(createRequest.getAvatar());
            } else {
                user.setAvatar(null); // Frontend will handle default avatar display
            }

            // Set company if provided
            if (createRequest.getCompanyId() != null) {
                try {
                    Company company = entityManager.getReference(Company.class, createRequest.getCompanyId());
                    user.setCompany(company);
                    logger.info("Assigned company ID: {} to user", createRequest.getCompanyId());
                } catch (Exception ex) {
                    logger.warn("Invalid company ID: {}", createRequest.getCompanyId());
                    throw new IllegalArgumentException("Invalid company ID: " + createRequest.getCompanyId());
                }
            }

            // Set team if provided
            if (createRequest.getTeamId() != null) {
                Team team = teamRepository.findById(createRequest.getTeamId())
                        .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + createRequest.getTeamId()));
                user.setTeam(team);
                logger.info("Assigned team ID: {} to user", createRequest.getTeamId());
            }

            // Save user
            User savedUser = userRepository.save(user);
            logger.info("User created successfully by admin with ID: {}", savedUser.getUserId());

            // Convert to response DTO
            return userMapper.toUserResponse(savedUser);

        } catch (Exception ex) {
            logger.error("Error during admin user creation: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to create user: " + ex.getMessage());
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
        if (updateRequest.getEmail() != null && !user.getEmail().equals(updateRequest.getEmail())) {
            if (isEmailExists(updateRequest.getEmail(), userId)) {
                throw new IllegalArgumentException("Email already exists");
            }
        }
        
        // Update user fields only if provided
        if (updateRequest.getFullName() != null) {
            user.setFullName(updateRequest.getFullName());
        }
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getAddress() != null) {
            user.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getGender() != null) {
            user.setGender(updateRequest.getGender());
        }
        if (updateRequest.getAvatar() != null) {
            user.setAvatar(updateRequest.getAvatar());
        }
        if (updateRequest.getCompanyId() != null) {
            // Find company by ID and set it
            Company company = companyRepository.findById(updateRequest.getCompanyId())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + updateRequest.getCompanyId()));
            user.setCompany(company);
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

    @Override
    @Transactional
    public UserResponse updateUserRole(UUID userId, String roleName) {
        logger.info("Updating user role for user ID: {} to role: {}", userId, roleName);
        
        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        if (user.getStatus() == UserStatus.DELETED) {
            throw new IllegalArgumentException("Cannot update role for deleted user");
        }
        
        // Validate role
        if (!isValidRole(roleName)) {
            throw new IllegalArgumentException("Invalid role. Allowed: EVENT_MANAGER or COLLABORATOR");
        }
        
        // Find role entity
        Role role = roleRepository.findByRoleName(roleName.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        
        // Update user role
        user.setRole(role);
        
        // Save user
        User updatedUser = userRepository.save(user);
        logger.info("User role updated successfully for user ID: {} to role: {}", userId, roleName);
        
        return userMapper.toUserResponse(updatedUser);
    }
    
    private boolean isValidRole(String roleName) {
        return "EVENT_MANAGER".equalsIgnoreCase(roleName) ||
               "COLLABORATOR".equalsIgnoreCase(roleName);
    }

    /**
     * Check if email already exists in database
     * @param email the email to check
     * @param excludeUserId optional user ID to exclude from check (for update operations)
     * @return true if email exists, false otherwise
     */
    private boolean isEmailExists(String email, UUID excludeUserId) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        Optional<User> existingUser = userRepository.findByEmail(email.trim());
        
        if (existingUser.isPresent()) {
            // If excludeUserId is provided, check if it's the same user
            if (excludeUserId != null && existingUser.get().getUserId().equals(excludeUserId)) {
                return false; // Same user, email is not duplicated
            }
            return true; // Email exists for different user
        }
        
        return false; // Email doesn't exist
    }

    @Override
    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        Optional<User> existingUser = userRepository.findByEmail(email.trim());
        return existingUser.isPresent();
    }

    @Override
    @Transactional
    public LoginResponse processGoogleOAuthCallback(String code, String state) {
        logger.info("Processing Google OAuth callback with code: {}", code);

        try {
            // Step 1: Exchange authorization code for access token
            String accessToken = exchangeCodeForAccessToken(code);

            // Step 2: Get user info from Google using access token
            GoogleUserInfo googleUserInfo = getUserInfoFromGoogle(accessToken);

            // Step 3: Find or create user in database
            User user = findOrCreateGoogleUser(googleUserInfo);

            // Step 4: Generate JWT token
            String jwtToken = jwtTokenProvider.generateToken(user);

            // Step 5: Create login response
            LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .user(userMapper.toUserResponse(user))
                .build();

            logger.info("Google OAuth login successful for user: {}", user.getEmail());
            return loginResponse;

        } catch (Exception e) {
            logger.error("Failed to process Google OAuth callback: {}", e.getMessage(), e);
            throw new RuntimeException("Google OAuth authentication failed: " + e.getMessage(), e);
        }
    }

    private String exchangeCodeForAccessToken(String code) throws Exception {
        logger.info("Exchanging authorization code for access token");

        try {
            // Create HTTP client
            RestTemplate restTemplate = new RestTemplate();

            // Build the correct redirect URI
            String baseUrl = "https://api.s-pace.com.vn";
            String redirectUri = baseUrl + "/api/auth/google/callback";

            // Prepare request parameters
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", googleClientId);
            params.add("client_secret", googleClientSecret);
            params.add("code", code);
            params.add("grant_type", "authorization_code");
            params.add("redirect_uri", redirectUri);

            logger.info("Using redirect URI: {}", redirectUri);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // Make request to Google token endpoint
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://oauth2.googleapis.com/token",
                    request,
                    String.class
            );

            logger.info("Google token response status: {}", response.getStatusCode());
            logger.debug("Google token response body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                // Parse response to get access token
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(response.getBody());
                
                if (jsonNode.has("access_token")) {
                    String accessToken = jsonNode.get("access_token").asText();
                    logger.info("Successfully exchanged code for access token");
                    return accessToken;
                } else {
                    logger.error("No access_token in response: {}", response.getBody());
                    throw new RuntimeException("No access_token in Google response");
                }
            } else {
                logger.error("Failed to exchange code for token. Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to exchange code for token: " + response.getStatusCode() + 
                    ", Response: " + response.getBody());
            }

        } catch (Exception e) {
            logger.error("Error exchanging code for access token: {}", e.getMessage(), e);
            throw new Exception("Failed to exchange authorization code for access token: " + e.getMessage(), e);
        }
    }

    private GoogleUserInfo getUserInfoFromGoogle(String accessToken) throws Exception {
        logger.info("Getting user info from Google");

        try {
            // Create HTTP client
            RestTemplate restTemplate = new RestTemplate();

            // Set headers with access token
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            // Make request to Google userinfo endpoint
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v2/userinfo",
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // Parse response to get user info
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(response.getBody());

                GoogleUserInfo userInfo = new GoogleUserInfo();
                userInfo.setId(jsonNode.get("id").asText());
                userInfo.setEmail(jsonNode.get("email").asText());
                userInfo.setName(jsonNode.get("name").asText());
                userInfo.setPicture(jsonNode.get("picture").asText());
                userInfo.setGivenName(jsonNode.has("given_name") ? jsonNode.get("given_name").asText() : null);
                userInfo.setFamilyName(jsonNode.has("family_name") ? jsonNode.get("family_name").asText() : null);
                userInfo.setLocale(jsonNode.has("locale") ? jsonNode.get("locale").asText() : null);
                userInfo.setVerifiedEmail(jsonNode.has("verified_email") ? jsonNode.get("verified_email").asBoolean() : false);

                logger.info("Successfully retrieved user info from Google: {}", userInfo.getEmail());
                return userInfo;
            } else {
                throw new RuntimeException("Failed to get user info from Google: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Error getting user info from Google: {}", e.getMessage(), e);
            throw new Exception("Failed to get user info from Google", e);
        }
    }

    private User findOrCreateGoogleUser(GoogleUserInfo googleUserInfo) {
        logger.info("Finding or creating user for Google OAuth: {}", googleUserInfo.getEmail());

        // First, try to find user by email
        Optional<User> existingUser = userRepository.findByEmail(googleUserInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user info if needed
            if (user.getAvatar() == null && googleUserInfo.getPicture() != null) {
                user.setAvatar(googleUserInfo.getPicture());
                userRepository.save(user);
            }
            logger.info("Found existing user by email: {}", user.getEmail());
            return user;
        }

        // Create new user
        Role collaboratorRole = roleRepository.findByRoleName("COLLABORATOR")
                .orElseThrow(() -> new RuntimeException("COLLABORATOR role not found"));

        User newUser = new User();
        newUser.setEmail(googleUserInfo.getEmail());
        newUser.setFullName(googleUserInfo.getName());
        newUser.setAvatar(googleUserInfo.getPicture());
        newUser.setRole(collaboratorRole);
        newUser.setStatus(UserStatus.ACTIVE);
        // Set a random password for OAuth users (they won't use it)
        newUser.setPasswordHash("OAUTH_USER");

        User savedUser = userRepository.save(newUser);
        logger.info("Created new user for Google OAuth: {}", savedUser.getEmail());
        return savedUser;
    }

    // Helper class for Google user info
    private static class GoogleUserInfo {
        private String id;
        private String email;
        private String name;
        private String picture;
        private String givenName;
        private String familyName;
        private String locale;
        private boolean verifiedEmail;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }
        public String getGivenName() { return givenName; }
        public void setGivenName(String givenName) { this.givenName = givenName; }
        public String getFamilyName() { return familyName; }
        public void setFamilyName(String familyName) { this.familyName = familyName; }
        public String getLocale() { return locale; }
        public void setLocale(String locale) { this.locale = locale; }
        public boolean isVerifiedEmail() { return verifiedEmail; }
        public void setVerifiedEmail(boolean verifiedEmail) { this.verifiedEmail = verifiedEmail; }
    }
}
