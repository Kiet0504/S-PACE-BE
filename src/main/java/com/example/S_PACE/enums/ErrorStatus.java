package com.example.S_PACE.enums;

public enum ErrorStatus {
    // General errors
    NULL_VALUE(500, "Internal server error", "A required field is missing or null"),
    INTERNAL_ERROR(500, "Internal server error", "An unexpected error occurred"),
    
    // Authentication errors
    INVALID_CREDENTIALS(401, "Authentication failed", "Invalid email or password"),
    USER_NOT_FOUND(404, "User not found", "User with the provided email does not exist"),
    USER_ALREADY_EXISTS(409, "User already exists", "User with this email already exists"),
    ACCOUNT_INACTIVE(403, "Account inactive", "Account is inactive or suspended"),
    
    // Registration errors
    REGISTRATION_FAILED(400, "Registration failed", "Failed to register user"),
    ROLE_NOT_FOUND(500, "Role not found", "No suitable role found in database"),
    
    // Database errors
    DATABASE_ERROR(500, "Database error", "Database operation failed"),
    TRANSACTION_ERROR(500, "Transaction error", "Database transaction failed"),
    
    // Validation errors
    INVALID_INPUT(400, "Invalid input", "Invalid request data provided"),
    MISSING_FIELDS(400, "Missing fields", "Required fields are missing");
    
    private final int code;
    private final String message;
    private final String description;
    
    ErrorStatus(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getDescription() {
        return description;
    }
}
