package com.recruitment.system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobStatsResponse {
    private Long jobId;
    private String jobTitle;
    private Long applicationsCount;
    private Long interviewsCount;
    private Long hiresCount;
    private Double conversionRate;
}
