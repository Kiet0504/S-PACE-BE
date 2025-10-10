package com.example.S_PACE.enums;

public enum EmailType {
    REGISTRATION_CONFIRMATION,  // Xác nhận đăng ký sự kiện
    REGISTRATION_APPROVED,      // Xác nhận cộng tác viên được duyệt
    EVENT_REMINDER,             // Nhắc nhở trước sự kiện
    EVENT_CANCELLATION,         // Hủy sự kiện
    FEEDBACK_REQUEST,           // Yêu cầu phản hồi sau sự kiện
    CERTIFICATE_ISSUED,         // Gửi chứng chỉ
    SYSTEM_NOTIFICATION         // Các thông báo hệ thống khác
}
