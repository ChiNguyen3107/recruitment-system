package com.recruitment.system.dto.request;

import com.recruitment.system.enums.InterviewType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewScheduleRequest {
    private Long applicationId;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private InterviewType interviewType;
    private String location;
    private String meetingLink;
    private String notes;
}



