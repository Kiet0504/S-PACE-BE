package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.AttendanceLogsRequest;
import com.example.S_PACE.dto.response.AttendanceLogsResponse;
import com.example.S_PACE.enums.AttendanceStatus;
import com.example.S_PACE.pojo.AttendanceLogs;
import com.example.S_PACE.pojo.Event;
import com.example.S_PACE.pojo.User;
import com.example.S_PACE.repository.AttendanceLogsRepository;
import com.example.S_PACE.repository.EventRepository;
import com.example.S_PACE.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttendanceLogsServiceImpl implements AttendanceLogsService {

    private final AttendanceLogsRepository attendanceLogsRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public AttendanceLogsServiceImpl(AttendanceLogsRepository attendanceLogsRepository,
                                     EventRepository eventRepository,
                                     UserRepository userRepository) {
        this.attendanceLogsRepository = attendanceLogsRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AttendanceLogsResponse createAttendanceLog(AttendanceLogsRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + request.getEventId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        AttendanceLogs attendanceLog = new AttendanceLogs();
        attendanceLog.setEvent(event);
        attendanceLog.setUser(user);
        attendanceLog.setCheckInTime(request.getCheckInTime());
        attendanceLog.setCheckOutTime(request.getCheckOutTime());
        attendanceLog.setStatus(request.getStatus());

        AttendanceLogs savedLog = attendanceLogsRepository.save(attendanceLog);
        return mapToResponse(savedLog);
    }

    @Override
    public AttendanceLogsResponse getAttendanceLogById(UUID attendanceLogsId) {
        AttendanceLogs attendanceLog = attendanceLogsRepository.findById(attendanceLogsId)
                .orElseThrow(() -> new RuntimeException("Attendance log not found with id: " + attendanceLogsId));
        return mapToResponse(attendanceLog);
    }

    @Override
    public List<AttendanceLogsResponse> getAllAttendanceLogs() {
        return attendanceLogsRepository.findAllWithEventAndUser().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceLogsResponse> getAttendanceLogsByEventId(UUID eventId) {
        return attendanceLogsRepository.findByEventEventId(eventId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceLogsResponse> getAttendanceLogsByUserId(UUID userId) {
        return attendanceLogsRepository.findByUserUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceLogsResponse> getAttendanceLogsByStatus(AttendanceStatus status) {
        return attendanceLogsRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceLogsResponse> getAttendanceLogsByEventIdAndStatus(UUID eventId, AttendanceStatus status) {
        return attendanceLogsRepository.findByEventIdAndStatus(eventId, status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceLogsResponse updateAttendanceLog(UUID attendanceLogsId, AttendanceLogsRequest request) {
        AttendanceLogs attendanceLog = attendanceLogsRepository.findById(attendanceLogsId)
                .orElseThrow(() -> new RuntimeException("Attendance log not found with id: " + attendanceLogsId));

        if (request.getEventId() != null) {
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new RuntimeException("Event not found with id: " + request.getEventId()));
            attendanceLog.setEvent(event);
        }

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
            attendanceLog.setUser(user);
        }

        if (request.getCheckInTime() != null) {
            attendanceLog.setCheckInTime(request.getCheckInTime());
        }

        if (request.getCheckOutTime() != null) {
            attendanceLog.setCheckOutTime(request.getCheckOutTime());
        }

        if (request.getStatus() != null) {
            attendanceLog.setStatus(request.getStatus());
        }

        AttendanceLogs updatedLog = attendanceLogsRepository.save(attendanceLog);
        return mapToResponse(updatedLog);
    }

    @Override
    @Transactional
    public void deleteAttendanceLog(UUID attendanceLogsId) {
        if (!attendanceLogsRepository.existsById(attendanceLogsId)) {
            throw new RuntimeException("Attendance log not found with id: " + attendanceLogsId);
        }
        attendanceLogsRepository.deleteById(attendanceLogsId);
    }

    @Override
    @Transactional
    public AttendanceLogsResponse checkIn(UUID eventId, UUID userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if already checked in
        List<AttendanceLogs> existingLogs = attendanceLogsRepository.findByEventIdAndUserId(eventId, userId);
        if (!existingLogs.isEmpty()) {
            throw new RuntimeException("User already has an attendance record for this event");
        }

        AttendanceLogs attendanceLog = new AttendanceLogs();
        attendanceLog.setEvent(event);
        attendanceLog.setUser(user);
        attendanceLog.setCheckInTime(LocalDateTime.now());
        attendanceLog.setStatus(AttendanceStatus.CHECKED_IN);

        AttendanceLogs savedLog = attendanceLogsRepository.save(attendanceLog);
        return mapToResponse(savedLog);
    }

    @Override
    @Transactional
    public AttendanceLogsResponse checkOut(UUID attendanceLogsId) {
        AttendanceLogs attendanceLog = attendanceLogsRepository.findById(attendanceLogsId)
                .orElseThrow(() -> new RuntimeException("Attendance log not found with id: " + attendanceLogsId));

        if (attendanceLog.getCheckOutTime() != null) {
            throw new RuntimeException("User already checked out");
        }

        attendanceLog.setCheckOutTime(LocalDateTime.now());
        attendanceLog.setStatus(AttendanceStatus.CHECKED_OUT);

        AttendanceLogs updatedLog = attendanceLogsRepository.save(attendanceLog);
        return mapToResponse(updatedLog);
    }

    private AttendanceLogsResponse mapToResponse(AttendanceLogs attendanceLog) {
        return AttendanceLogsResponse.builder()
                .attendanceLogsId(attendanceLog.getAttendanceLogsId())
                .eventId(attendanceLog.getEvent().getEventId())
                .eventName(attendanceLog.getEvent().getTitle())
                .userId(attendanceLog.getUser().getUserId())
                .userName(attendanceLog.getUser().getFullName())
                .userEmail(attendanceLog.getUser().getEmail())
                .checkInTime(attendanceLog.getCheckInTime())
                .checkOutTime(attendanceLog.getCheckOutTime())
                .status(attendanceLog.getStatus())
                .build();
    }
}
