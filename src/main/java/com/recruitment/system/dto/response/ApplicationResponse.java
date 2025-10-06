package com.recruitment.system.dto.response;

import com.recruitment.system.enums.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO response cho đơn ứng tuyển
 */
@Data
public class ApplicationResponse {

    private Long id;
    private ApplicationStatus status;
    private String coverLetter;
    private String resumeUrl;
    private String additionalDocuments;
    private LocalDateTime interviewDate;
    private String interviewLocation;
    private String interviewNotes;
    private String feedback;
    private String rejectionReason;
    private String offerDetails;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Thông tin tin tuyển dụng
    private JobPostingResponse jobPosting;
    
    // Thông tin ứng viên
    private UserResponse applicant;
    
    // Trạng thái
    private Boolean isReviewed;
    private Boolean isInProgress;
    private Boolean isCompleted;

    // Timeline
    private java.util.List<ApplicationTimelineResponse> timeline;
}