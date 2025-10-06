package com.recruitment.system.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO response cho thông tin công ty
 */
@Data
public class CompanyResponse {

    private Long id;
    private String name;
    private String description;
    private String businessLicense;
    private String taxCode;
    private String website;
    private String industry;
    private String companySize;
    private String address;
    private String city;
    private String country;
    private String phoneNumber;
    private String contactEmail;
    private String logoUrl;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private Integer employeeCount;
    private Integer jobPostingCount;

    // Bổ sung cho hồ sơ công ty công khai
    private List<String> benefits;
    private String workingHours;
    private List<String> companyPhotos;
    private Map<String, String> socialLinks;
    // companySize đã có phía trên
    private Integer activeJobsCount;
    private Double averageResponseTime; // ngày
    private Double hiringSuccessRate; // hired / total
}