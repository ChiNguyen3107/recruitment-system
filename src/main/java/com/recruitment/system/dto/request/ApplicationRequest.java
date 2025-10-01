package com.recruitment.system.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * DTO cho nộp đơn ứng tuyển
 */
@Data
public class ApplicationRequest {

    @NotNull(message = "ID tin tuyển dụng không được để trống")
    private Long jobPostingId;

    @Size(max = 5000, message = "Thư xin việc không được quá 5000 ký tự")
    private String coverLetter;

    @Pattern(regexp = "^(https?://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?)?$", 
             message = "URL CV không hợp lệ")
    @Size(max = 500, message = "URL CV không được quá 500 ký tự")
    private String resumeUrl;

    @Size(max = 1000, message = "Tài liệu bổ sung không được quá 1000 ký tự")
    private String additionalDocuments;

    /**
     * Sanitize input để tránh XSS và các vấn đề bảo mật
     */
    public void sanitize() {
        if (StringUtils.hasText(coverLetter)) {
            coverLetter = sanitizeText(coverLetter);
        }
        if (StringUtils.hasText(additionalDocuments)) {
            additionalDocuments = sanitizeText(additionalDocuments);
        }
    }

    private String sanitizeText(String text) {
        if (text == null) return null;
        
        return text
            .replaceAll("<script[^>]*>.*?</script>", "") // Loại bỏ script tags
            .replaceAll("<[^>]*>", "") // Loại bỏ tất cả HTML tags
            .replaceAll("javascript:", "") // Loại bỏ javascript: protocol
            .replaceAll("on\\w+\\s*=", "") // Loại bỏ event handlers
            .trim();
    }
}