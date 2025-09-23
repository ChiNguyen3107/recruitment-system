package com.recruitment.system.dto.response;

import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.enums.JobType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO response cho tin tuyển dụng
 */
@Data
public class JobPostingResponse {

    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String benefits;
    private JobType jobType;
    private JobStatus status;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryCurrency;
    private String experienceRequired;
    private String educationRequired;
    private String skillsRequired;
    private Integer numberOfPositions;
    private LocalDateTime applicationDeadline;
    private LocalDateTime publishedAt;
    private Integer viewsCount;
    private Integer applicationsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin công ty
    private CompanyResponse company;
    
    // Thông tin người tạo
    private UserResponse createdBy;
    
    // Trạng thái có thể ứng tuyển
    private Boolean canApply;
    private Boolean isExpired;
}