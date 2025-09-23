package com.recruitment.system.dto.request;

import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.enums.JobType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho tạo/cập nhật tin tuyển dụng
 */
@Data
public class JobPostingRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề không được quá 200 ký tự")
    private String title;

    @NotBlank(message = "Mô tả công việc không được để trống")
    private String description;

    private String requirements;

    private String benefits;

    @NotNull(message = "Loại công việc không được để trống")
    private JobType jobType;

    private JobStatus status = JobStatus.DRAFT;

    @NotBlank(message = "Địa điểm làm việc không được để trống")
    private String location;

    @DecimalMin(value = "0", message = "Lương tối thiểu phải >= 0")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0", message = "Lương tối đa phải >= 0")
    private BigDecimal salaryMax;

    private String salaryCurrency = "VND";

    private String experienceRequired;

    private String educationRequired;

    private String skillsRequired;

    @Min(value = 1, message = "Số lượng tuyển phải >= 1")
    private Integer numberOfPositions = 1;

    @Future(message = "Hạn nộp đơn phải là thời gian tương lai")
    private LocalDateTime applicationDeadline;
}