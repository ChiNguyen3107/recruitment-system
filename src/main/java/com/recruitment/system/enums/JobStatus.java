package com.recruitment.system.enums;

/**
 * Trạng thái tin tuyển dụng
 */
public enum JobStatus {
    DRAFT("Bản nháp"),
    ACTIVE("Đang tuyển"),
    PAUSED("Tạm dừng"),
    CLOSED("Đã đóng"),
    EXPIRED("Hết hạn");

    private final String displayName;

    JobStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}