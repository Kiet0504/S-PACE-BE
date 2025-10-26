package com.example.S_PACE.validator;

import com.example.S_PACE.dto.request.CompanyAutoApprovalRuleRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CompanyAutoApprovalRuleValidator implements ConstraintValidator<ValidCompanyAutoApprovalRule, CompanyAutoApprovalRuleRequest> {

    private static final List<String> VALID_FIELD_NAMES = Arrays.asList(
        "company_name", "address"
    );

    private static final List<String> VALID_FIELD_TYPES = Arrays.asList(
        "STRING", "NUMBER", "BOOLEAN", "DATE"
    );

    private static final List<String> VALID_OPERATORS = Arrays.asList(
        "NOT_EMPTY", "LENGTH_GREATER_THAN", "LENGTH_LESS_THAN", 
        "REGEX_MATCH", "EQUALS", "CONTAINS", "GREATER_THAN", 
        "LESS_THAN", "BETWEEN", "IN", "NOT_IN"
    );

    @Override
    public void initialize(ValidCompanyAutoApprovalRule constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(CompanyAutoApprovalRuleRequest request, ConstraintValidatorContext context) {
        boolean isValid = true;

        // Validate field name
        if (request.getFieldName() != null && !VALID_FIELD_NAMES.contains(request.getFieldName())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid field name. Valid fields: " + VALID_FIELD_NAMES)
                    .addPropertyNode("fieldName")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate field type
        if (request.getFieldType() != null && !VALID_FIELD_TYPES.contains(request.getFieldType())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid field type. Valid types: " + VALID_FIELD_TYPES)
                    .addPropertyNode("fieldType")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate operator
        if (request.getOperator() != null && !VALID_OPERATORS.contains(request.getOperator())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid operator. Valid operators: " + VALID_OPERATORS)
                    .addPropertyNode("operator")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate operator and field type compatibility
        if (request.getOperator() != null && request.getFieldType() != null) {
            if (!isOperatorCompatibleWithFieldType(request.getOperator(), request.getFieldType())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Operator '" + request.getOperator() + 
                        "' is not compatible with field type '" + request.getFieldType() + "'")
                        .addPropertyNode("operator")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        // Validate expected value based on operator
        if (request.getOperator() != null && request.getExpectedValue() != null) {
            if (!isExpectedValueValidForOperator(request.getOperator(), request.getExpectedValue())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Invalid expected value for operator '" + 
                        request.getOperator() + "'")
                        .addPropertyNode("expectedValue")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean isOperatorCompatibleWithFieldType(String operator, String fieldType) {
        switch (fieldType) {
            case "STRING":
                return Arrays.asList("NOT_EMPTY", "LENGTH_GREATER_THAN", "LENGTH_LESS_THAN", 
                                   "REGEX_MATCH", "EQUALS", "CONTAINS", "IN", "NOT_IN").contains(operator);
            case "NUMBER":
                return Arrays.asList("GREATER_THAN", "LESS_THAN", "EQUALS", "BETWEEN", 
                                   "IN", "NOT_IN").contains(operator);
            case "BOOLEAN":
                return Arrays.asList("EQUALS").contains(operator);
            case "DATE":
                return Arrays.asList("GREATER_THAN", "LESS_THAN", "EQUALS", "BETWEEN").contains(operator);
            default:
                return false;
        }
    }

    private boolean isExpectedValueValidForOperator(String operator, String expectedValue) {
        switch (operator) {
            case "LENGTH_GREATER_THAN":
            case "LENGTH_LESS_THAN":
            case "GREATER_THAN":
            case "LESS_THAN":
                try {
                    Integer.parseInt(expectedValue);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "REGEX_MATCH":
                try {
                    Pattern.compile(expectedValue);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            case "BETWEEN":
                // Expected format: "min,max"
                String[] parts = expectedValue.split(",");
                if (parts.length != 2) return false;
                try {
                    Integer.parseInt(parts[0].trim());
                    Integer.parseInt(parts[1].trim());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "IN":
            case "NOT_IN":
                // Expected format: "value1,value2,value3"
                return !expectedValue.trim().isEmpty();
            default:
                return true; // For other operators, any value is acceptable
        }
    }
}
