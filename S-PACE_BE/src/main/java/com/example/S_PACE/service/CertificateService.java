package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.CertificateRequest;
import com.example.S_PACE.pojo.Certificates;

import java.util.List;
import java.util.UUID;

public interface CertificateService {
    
    Certificates createCertificate(CertificateRequest request, String filePath);
    
    Certificates getCertificateById(UUID certificateId);
    
    List<Certificates> getCertificatesByEvent(UUID eventId);
    
    List<Certificates> getCertificatesByUser(UUID userId);
    
    Certificates getCertificateByEventAndUser(UUID eventId, UUID userId);
    
    Certificates updateCertificate(UUID certificateId, CertificateRequest request, String filePath);
    
    void deleteCertificate(UUID certificateId);
    
    boolean existsByEventAndUser(UUID eventId, UUID userId);
    
    boolean existsByCertificateCode(String certificateCode);
}
