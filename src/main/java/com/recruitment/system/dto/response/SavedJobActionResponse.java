package com.recruitment.system.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SavedJobActionResponse {
    private  Long jobId;
    private String action; //"saved"|"unsaved"
    private LocalDateTime savedAt; //chỉ có khi "saved"
}