package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.CertificateRequest;
import com.example.S_PACE.pojo.Certificates;
import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.CertificateRepository;
import com.example.S_PACE.repository.EventRepository;
import com.example.S_PACE.repository.UserRepository;
import com.example.S_PACE.service.CertificateService;
import com.example.S_PACE.service.CloudStorageService;
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

    @Autowired(required = false)
    private CloudStorageService cloudStorageService;

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
        Certificates certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found with ID: " + certificateId));
        populatePresignedUrl(certificate);
        return certificate;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Certificates> getCertificatesByEvent(UUID eventId) {
        logger.info("Fetching certificates for event: {}", eventId);
        List<Certificates> certificates = certificateRepository.findByEventEventId(eventId);
        certificates.forEach(this::populatePresignedUrl);
        return certificates;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Certificates> getCertificatesByUser(UUID userId) {
        logger.info("Fetching certificates for user: {}", userId);
        List<Certificates> certificates = certificateRepository.findByUserUserId(userId);
        certificates.forEach(this::populatePresignedUrl);
        return certificates;
    }

    @Override
    @Transactional(readOnly = true)
    public Certificates getCertificateByEventAndUser(UUID eventId, UUID userId) {
        logger.info("Fetching certificate for user: {} in event: {}", userId, eventId);
        Certificates certificate = certificateRepository.findCertificateByEventAndUser(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found for user in this event"));
        populatePresignedUrl(certificate);
        return certificate;
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

    private void populatePresignedUrl(Certificates certificate) {
        if (certificate == null) {
            return;
        }

        String filePath = certificate.getCertificateFilePath();
        if (filePath != null && !filePath.isEmpty() && cloudStorageService != null) {
            try {
                if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                    String s3Key = extractS3KeyFromUrl(filePath);
                    String presignedUrl = cloudStorageService.generatePresignedUrl(s3Key);
                    certificate.setCertificatePresignedUrl(presignedUrl);
                } else {
                    String presignedUrl = cloudStorageService.generatePresignedUrl(filePath);
                    certificate.setCertificatePresignedUrl(presignedUrl);
                }
                logger.debug("Generated pre-signed URL for certificate: {}", certificate.getCertificatesId());
            } catch (Exception e) {
                logger.warn("Failed to generate pre-signed URL for certificate {}: {}",
                    certificate.getCertificatesId(), e.getMessage());
                certificate.setCertificatePresignedUrl(filePath);
            }
        }
    }

    private String extractS3KeyFromUrl(String url) {
        try {
            if (url.contains(".s3.amazonaws.com/")) {
                return url.substring(url.indexOf(".s3.amazonaws.com/") + 18);
            } else if (url.contains("s3.amazonaws.com/")) {
                String[] parts = url.split("s3.amazonaws.com/");
                if (parts.length > 1) {
                    String[] keyParts = parts[1].split("/", 2);
                    return keyParts.length > 1 ? keyParts[1] : parts[1];
                }
            }
            return url;
        } catch (Exception e) {
            logger.error("Failed to extract S3 key from URL: {}", url, e);
            return url;
        }
    }
}
