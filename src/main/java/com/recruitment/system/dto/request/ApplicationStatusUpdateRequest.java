package com.recruitment.system.dto.request;

import com.recruitment.system.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho yêu cầu cập nhật trạng thái đơn ứng tuyển
 */
@Data
public class ApplicationStatusUpdateRequest {

    @NotNull
    private ApplicationStatus status;

    // Ghi chú kèm theo (ví dụ: lý do từ chối, chi tiết offer, ghi chú PV)
    private String notes;
}



