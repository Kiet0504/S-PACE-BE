package com.example.S_PACE.service.impl;

import com.example.S_PACE.enums.EmailType;
import com.example.S_PACE.pojo.EmailNotifications;
import com.example.S_PACE.pojo.EventRegistration;
import com.example.S_PACE.repository.EmailNotificationsRepository;
import com.example.S_PACE.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EmailNotificationsRepository emailNotificationsRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendRegistrationApprovedEmail(EventRegistration eventRegistration) {
        try {
            logger.info("Preparing to send approval email to user: {} for event: {}",
                eventRegistration.getUser().getEmail(),
                eventRegistration.getEvent().getTitle());

            // Prepare email context
            Context context = new Context();
            context.setVariable("userName", eventRegistration.getUser().getFullName());
            context.setVariable("eventTitle", eventRegistration.getEvent().getTitle());
            context.setVariable("eventDate", eventRegistration.getEvent().getStartDate());
            context.setVariable("eventLocation", eventRegistration.getEvent().getLocation());
            context.setVariable("reviewNotes", eventRegistration.getReviewNotes());

            // Process email template
            String emailContent = templateEngine.process("email/registration-approved", context);

            // Create email message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(eventRegistration.getUser().getEmail());
            helper.setSubject("Chúc mừng! Đơn đăng ký sự kiện của bạn đã được duyệt");
            helper.setText(emailContent, true);

            // Send email
            mailSender.send(message);
            logger.info("Approval email sent successfully to: {}", eventRegistration.getUser().getEmail());

            // Save email notification record
            saveEmailNotification(eventRegistration, emailContent);

        } catch (MessagingException e) {
            logger.error("Failed to send approval email to: {}. Error: {}",
                eventRegistration.getUser().getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send approval email", e);
        }
    }

    private void saveEmailNotification(EventRegistration eventRegistration, String emailContent) {
        try {
            EmailNotifications notification = new EmailNotifications();
            notification.setUser(eventRegistration.getUser());
            notification.setEvent(eventRegistration.getEvent());
            notification.setEventRegistration(eventRegistration);
            notification.setEmailType(EmailType.REGISTRATION_APPROVED);
            notification.setSubject("Chúc mừng! Đơn đăng ký sự kiện của bạn đã được duyệt");
            notification.setContent(emailContent);

            emailNotificationsRepository.save(notification);
            logger.info("Email notification record saved for registration: {}",
                eventRegistration.getEventRegistrationId());
        } catch (Exception e) {
            logger.error("Failed to save email notification record: {}", e.getMessage(), e);
        }
    }
}
