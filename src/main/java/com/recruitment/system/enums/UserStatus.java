package com.recruitment.system.enums;

/**
 * Trạng thái tài khoản người dùng
 */
public enum UserStatus {
    ACTIVE("Hoạt động"),
    INACTIVE("Không hoạt động"),
    PENDING("Chờ xác thực"),
    SUSPENDED("Bị khóa"),
    DELETED("Đã xóa");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}