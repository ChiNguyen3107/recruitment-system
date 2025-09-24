package com.recruitment.system.dto.request;

import com.recruitment.system.enums.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho cập nhật trạng thái tin tuyển dụng
 */
@Data
public class JobStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private JobStatus status;
}


