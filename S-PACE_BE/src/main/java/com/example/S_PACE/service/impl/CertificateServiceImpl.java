package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.CertificateRequest;
import com.example.S_PACE.pojo.Certificates;
import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.CertificateRepository;
import com.example.S_PACE.repository.EventRepository;
import com.example.S_PACE.repository.UserRepository;
import com.example.S_PACE.service.CertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CertificateServiceImpl implements CertificateService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateServiceImpl.class);

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Certificates createCertificate(CertificateRequest request, String filePath) {
        logger.info("Creating certificate for user: {} in event: {}", request.getUserId(), request.getEventId());

        // Validate event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + request.getEventId()));

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUserId()));

        // Check if certificate already exists for this event and user
        if (certificateRepository.existsByEventEventIdAndUserUserId(request.getEventId(), request.getUserId())) {
            throw new IllegalArgumentException("Certificate already exists for this user in this event");
        }

        // Check if certificate code is unique
        if (certificateRepository.existsByCertificateCode(request.getCertificateCode())) {
            throw new IllegalArgumentException("Certificate code already exists: " + request.getCertificateCode());
        }

        // Create certificate
        Certificates certificate = new Certificates();
        certificate.setEvent(event);
        certificate.setUser(user);
        certificate.setCertificateFilePath(filePath);
        certificate.setCertificateCode(request.getCertificateCode());
        certificate.setIssuedDate(request.getIssuedDate());
        certificate.setIssuedBy(request.getIssuedBy());

        Certificates savedCertificate = certificateRepository.save(certificate);
        logger.info("Certificate created successfully with ID: {}", savedCertificate.getCertificatesId());

        return savedCertificate;
    }

    @Override
    @Transactional(readOnly = true)
    public Certificates getCertificateById(UUID certificateId) {
        logger.info("Fetching certificate by ID: {}", certificateId);
        return certificateRepository.findById(certificateId)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found with ID: " + certificateId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Certificates> getCertificatesByEvent(UUID eventId) {
        logger.info("Fetching certificates for event: {}", eventId);
        return certificateRepository.findByEventEventId(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Certificates> getCertificatesByUser(UUID userId) {
        logger.info("Fetching certificates for user: {}", userId);
        return certificateRepository.findByUserUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Certificates getCertificateByEventAndUser(UUID eventId, UUID userId) {
        logger.info("Fetching certificate for user: {} in event: {}", userId, eventId);
        return certificateRepository.findCertificateByEventAndUser(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found for user in this event"));
    }

    @Override
    public Certificates updateCertificate(UUID certificateId, CertificateRequest request, String filePath) {
        logger.info("Updating certificate with ID: {}", certificateId);

        Certificates certificate = getCertificateById(certificateId);

        // Update fields
        if (filePath != null && !filePath.isEmpty()) {
            certificate.setCertificateFilePath(filePath);
        }
        certificate.setCertificateCode(request.getCertificateCode());
        certificate.setIssuedDate(request.getIssuedDate());
        certificate.setIssuedBy(request.getIssuedBy());

        Certificates updatedCertificate = certificateRepository.save(certificate);
        logger.info("Certificate updated successfully with ID: {}", updatedCertificate.getCertificatesId());

        return updatedCertificate;
    }

    @Override
    public void deleteCertificate(UUID certificateId) {
        logger.info("Deleting certificate with ID: {}", certificateId);

        if (!certificateRepository.existsById(certificateId)) {
            throw new IllegalArgumentException("Certificate not found with ID: " + certificateId);
        }

        certificateRepository.deleteById(certificateId);
        logger.info("Certificate deleted successfully with ID: {}", certificateId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEventAndUser(UUID eventId, UUID userId) {
        return certificateRepository.existsByEventEventIdAndUserUserId(eventId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCertificateCode(String certificateCode) {
        return certificateRepository.existsByCertificateCode(certificateCode);
    }
}
