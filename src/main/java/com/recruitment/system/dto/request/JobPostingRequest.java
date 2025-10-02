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
    @Size(max = 20000, message = "Mô tả không được quá 20000 ký tự")
    private String description;

    @Size(max = 10000, message = "Yêu cầu không được quá 10000 ký tự")
    private String requirements;

    @Size(max = 8000, message = "Quyền lợi không được quá 8000 ký tự")
    private String benefits;

    @NotNull(message = "Loại công việc không được để trống")
    private JobType jobType;

    private JobStatus status = JobStatus.DRAFT;

    @NotBlank(message = "Địa điểm làm việc không được để trống")
    @Size(max = 200, message = "Địa điểm không được quá 200 ký tự")
    private String location;

    @DecimalMin(value = "0", message = "Lương tối thiểu phải >= 0")
    private BigDecimal salaryMin;

    @DecimalMin(value = "0", message = "Lương tối đa phải >= 0")
    private BigDecimal salaryMax;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Tiền tệ phải là mã ISO 4217 (3 chữ cái)")
    private String salaryCurrency = "VND";

    @Size(max = 5000, message = "Kinh nghiệm không được quá 5000 ký tự")
    private String experienceRequired;

    @Size(max = 5000, message = "Học vấn không được quá 5000 ký tự")
    private String educationRequired;

    @Size(max = 5000, message = "Kỹ năng không được quá 5000 ký tự")
    private String skillsRequired;

    @Min(value = 1, message = "Số lượng tuyển phải >= 1")
    private Integer numberOfPositions = 1;

    @Future(message = "Hạn nộp đơn phải là thời gian tương lai")
    private LocalDateTime applicationDeadline;
}