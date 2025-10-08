package com.example.S_PACE.enums;

public enum CompanyStatus {
    PENDING_APPROVAL,    // Công ty đang chờ duyệt
    ACTIVE,              // Công ty đang hoạt động
    SUSPENDED,           // Công ty bị tạm ngưng
    INACTIVE,            // Công ty không hoạt động
    BLACKLISTED,         // Công ty bị đưa vào danh sách đen
    UNDER_REVIEW,        // Công ty đang được xem xét/kiểm tra
    EXPIRED,             // Hợp đồng/đăng ký của công ty đã hết hạn
    DELETED;             // Công ty đã bị xóa


    // Helper methods
    public boolean canCreateEvents() {
        return this == ACTIVE;
    }

    public boolean isOperational() {
        return this == ACTIVE || this == UNDER_REVIEW;
    }

    public boolean requiresAction() {
        return this == PENDING_APPROVAL || this == UNDER_REVIEW || this == EXPIRED;
    }

    public boolean isRestricted() {
        return this == SUSPENDED || this == BLACKLISTED || this == INACTIVE;
    }
}