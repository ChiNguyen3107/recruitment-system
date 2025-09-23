package com.recruitment.system.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO cho nộp đơn ứng tuyển
 */
@Data
public class ApplicationRequest {

    @NotNull(message = "ID tin tuyển dụng không được để trống")
    private Long jobPostingId;

    @Size(max = 5000, message = "Thư xin việc không được quá 5000 ký tự")
    private String coverLetter;

    private String resumeUrl;

    private String additionalDocuments;
}