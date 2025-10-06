package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.AttendanceLogsRequest;
import com.example.S_PACE.dto.response.AttendanceLogsResponse;
import com.example.S_PACE.enums.AttendanceStatus;

import java.util.List;
import java.util.UUID;

public interface AttendanceLogsService {
    AttendanceLogsResponse createAttendanceLog(AttendanceLogsRequest request);
    AttendanceLogsResponse getAttendanceLogById(UUID attendanceLogsId);
    List<AttendanceLogsResponse> getAllAttendanceLogs();
    List<AttendanceLogsResponse> getAttendanceLogsByEventId(UUID eventId);
    List<AttendanceLogsResponse> getAttendanceLogsByUserId(UUID userId);
    List<AttendanceLogsResponse> getAttendanceLogsByStatus(AttendanceStatus status);
    List<AttendanceLogsResponse> getAttendanceLogsByEventIdAndStatus(UUID eventId, AttendanceStatus status);
    AttendanceLogsResponse updateAttendanceLog(UUID attendanceLogsId, AttendanceLogsRequest request);
    void deleteAttendanceLog(UUID attendanceLogsId);
    AttendanceLogsResponse checkIn(UUID eventId, UUID userId);
    AttendanceLogsResponse checkOut(UUID attendanceLogsId);
}
