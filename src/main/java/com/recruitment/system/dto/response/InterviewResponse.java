package com.recruitment.system.dto.response;

import com.recruitment.system.enums.InterviewStatus;
import com.recruitment.system.enums.InterviewType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewResponse {
    private Long id;
    private Long applicationId;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String location;
    private String meetingLink;
    private InterviewType interviewType;
    private String notes;
    private InterviewStatus status;
    private Long scheduledBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



