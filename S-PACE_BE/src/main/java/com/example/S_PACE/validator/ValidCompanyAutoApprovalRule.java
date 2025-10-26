package com.example.S_PACE.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CompanyAutoApprovalRuleValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCompanyAutoApprovalRule {
    String message() default "Invalid company auto approval rule";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
