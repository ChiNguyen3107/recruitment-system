package com.recruitment.system.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CompanyUpdateRequest {

    @NotBlank
    @Size(min = 1, max = 200)
    private String name;

    @Size(max = 2000)
    private String description;

    @Pattern(regexp = "^(?:[0-2]?[0-9]:[0-5][0-9]-[0-2]?[0-9]:[0-5][0-9]|[Ff]lexible)$",
             message = "workingHours phải dạng 'HH:mm-HH:mm' hoặc 'Flexible'")
    private String workingHours;

    @Size(max = 10)
    private List<@Size(max = 100) String> benefits;

    private List<@Pattern(regexp = "^(https?://).+", message = "URL ảnh không hợp lệ") String> companyPhotos;

    private Map<@Pattern(regexp = "^[a-zA-Z0-9_-]{1,20}$", message = "Key mạng xã hội không hợp lệ") String,
                @Pattern(regexp = "^(https?://).+", message = "URL mạng xã hội không hợp lệ") String> socialLinks;

    @Pattern(regexp = "^(STARTUP|SMALL|MEDIUM|LARGE|ENTERPRISE)$",
             message = "companySize không hợp lệ")
    private String companySize;

    @Size(max = 191)
    @Pattern(regexp = "^(https?://).+", message = "Website không hợp lệ")
    private String website;

    @Size(max = 100)
    private String industry;

    @Size(max = 255)
    private String address;

    @Size(max = 120)
    private String city;

    @Size(max = 120)
    private String country;

    @Size(max = 20)
    private String phoneNumber;

    @Email
    @Size(max = 191)
    private String contactEmail;
}


