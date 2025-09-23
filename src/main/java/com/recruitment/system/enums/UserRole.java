package com.recruitment.system.enums;

/**
 * Enum đại diện cho các vai trò người dùng trong hệ thống
 */
public enum UserRole {
    ADMIN("Admin"),
    EMPLOYER("Nhà tuyển dụng"),
    RECRUITER("Nhân viên tuyển dụng"), 
    APPLICANT("Ứng viên"),
    GUEST("Khách");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}