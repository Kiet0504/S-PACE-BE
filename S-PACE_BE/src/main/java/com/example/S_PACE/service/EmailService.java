package com.example.S_PACE.service;

import com.example.S_PACE.pojo.EventRegistration;

public interface EmailService {

    /**
     * Sends an approval notification email to the collaborator
     * @param eventRegistration The event registration that was approved
     */
    void sendRegistrationApprovedEmail(EventRegistration eventRegistration);
}
