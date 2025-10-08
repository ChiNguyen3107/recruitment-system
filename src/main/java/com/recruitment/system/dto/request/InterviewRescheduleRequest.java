package com.recruitment.system.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewRescheduleRequest {
    private LocalDateTime newScheduledAt;
    private String reason;
}



