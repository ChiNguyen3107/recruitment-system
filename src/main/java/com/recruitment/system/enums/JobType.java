package com.recruitment.system.enums;

/**
 * Loại hình công việc
 */
public enum JobType {
    FULL_TIME("Toàn thời gian"),
    PART_TIME("Bán thời gian"),
    CONTRACT("Hợp đồng"),
    INTERNSHIP("Thực tập"),
    FREELANCE("Tự do");

    private final String displayName;

    JobType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}