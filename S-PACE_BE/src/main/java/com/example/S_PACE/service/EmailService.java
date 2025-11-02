package com.example.S_PACE.service;

import com.example.S_PACE.pojo.EventRegistration;

public interface EmailService {

    /**
     * Sends an approval notification email to the collaborator
     * @param eventRegistration The event registration that was approved
     */
    void sendRegistrationApprovedEmail(EventRegistration eventRegistration);

    /**
     * Sends a password reset email with reset link
     * @param toEmail The recipient email address
     * @param resetToken The password reset token
     */
    void sendPasswordResetEmail(String toEmail, String resetToken);
}
