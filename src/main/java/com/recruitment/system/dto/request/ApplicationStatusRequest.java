package com.recruitment.system.dto.request;

import com.recruitment.system.enums.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO cho cập nhật trạng thái đơn ứng tuyển
 */
@Data
public class ApplicationStatusRequest {

    private ApplicationStatus status;

    private String feedback;

    private String rejectionReason;

    private String offerDetails;

    private LocalDateTime interviewDate;

    private String interviewLocation;

    private String interviewNotes;
}