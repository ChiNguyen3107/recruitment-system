package com.recruitment.system.enums;

/**
 * Trạng thái đơn ứng tuyển
 */
public enum ApplicationStatus {
    RECEIVED("Đã nhận"),
    REVIEWED("Đã xem xét"),
    INTERVIEW("Phỏng vấn"),
    OFFER("Đề nghị"),
    HIRED("Được tuyển"),
    REJECTED("Từ chối"),
    WITHDRAWN("Đã rút đơn");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}