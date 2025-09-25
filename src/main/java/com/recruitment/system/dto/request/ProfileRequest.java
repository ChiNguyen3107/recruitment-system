package com.recruitment.system.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO cho cập nhật hồ sơ cá nhân
 */
@Data
public class ProfileRequest {

    private LocalDate dateOfBirth;

    @Size(max = 20, message = "Giới tính không được vượt quá 20 ký tự")
    private String gender;

    @Size(max = 200, message = "Địa chỉ không được vượt quá 200 ký tự")
    private String address;

    @Size(max = 100, message = "Thành phố không được vượt quá 100 ký tự")
    private String city;

    @Size(max = 100, message = "Quốc gia không được vượt quá 100 ký tự")
    private String country;

    @Size(max = 2000, message = "Tóm tắt không được vượt quá 2000 ký tự")
    private String summary;

    @Size(max = 5000, message = "Kinh nghiệm không được vượt quá 5000 ký tự")
    private String experience;

    @Size(max = 3000, message = "Học vấn không được vượt quá 3000 ký tự")
    private String education;

    @Size(max = 2000, message = "Kỹ năng không được vượt quá 2000 ký tự")
    private String skills;

    @Size(max = 2000, message = "Chứng chỉ không được vượt quá 2000 ký tự")
    private String certifications;

    @Size(max = 1000, message = "Ngôn ngữ không được vượt quá 1000 ký tự")
    private String languages;

    @Size(max = 500, message = "URL LinkedIn không được vượt quá 500 ký tự")
    private String linkedinUrl;

    @Size(max = 500, message = "URL GitHub không được vượt quá 500 ký tự")
    private String githubUrl;

    @Size(max = 500, message = "URL Portfolio không được vượt quá 500 ký tự")
    private String portfolioUrl;

    private Long desiredSalaryMin;

    private Long desiredSalaryMax;

    @Size(max = 100, message = "Loại công việc mong muốn không được vượt quá 100 ký tự")
    private String desiredJobType;

    @Size(max = 200, message = "Địa điểm mong muốn không được vượt quá 200 ký tự")
    private String desiredLocation;

    @Size(max = 200, message = "Tình trạng sẵn sàng không được vượt quá 200 ký tự")
    private String availability;

    private Boolean isPublic = false;
}