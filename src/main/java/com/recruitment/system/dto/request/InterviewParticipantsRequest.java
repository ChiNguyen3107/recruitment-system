package com.recruitment.system.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class InterviewParticipantsRequest {
    private List<Long> userIds;
    private String role; // optional default INTERVIEWER
}


