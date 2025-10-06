package com.recruitment.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entity đại diện cho công ty
 */
@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "business_license")
    private String businessLicense;

    @Column(name = "tax_code")
    private String taxCode;

    private String website;

    private String industry;

    @Column(name = "company_size")
    private String companySize;

    private String address;

    private String city;

    private String country;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    // Thông tin mở rộng cho hồ sơ công ty
    @Column(name = "benefits", columnDefinition = "TEXT")
    @Convert(converter = com.recruitment.system.entity.converter.StringListJsonConverter.class)
    private List<String> benefits;

    @Column(name = "working_hours")
    private String workingHours;

    @Column(name = "company_photos", columnDefinition = "TEXT")
    @Convert(converter = com.recruitment.system.entity.converter.StringListJsonConverter.class)
    private List<String> companyPhotos;

    @Column(name = "social_links", columnDefinition = "TEXT")
    @Convert(converter = com.recruitment.system.entity.converter.StringMapJsonConverter.class)
    private Map<String, String> socialLinks;

    // Các chỉ số tổng hợp (tính động, có thể lưu cache tầng ứng dụng; không bắt buộc lưu DB)
    @Transient
    private Integer activeJobsCount;

    @Transient
    private Double averageResponseTime;

    @Transient
    private Double hiringSuccessRate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Quan hệ với Users (employees of company)
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> employees = new ArrayList<>();

    // Quan hệ với JobPostings
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JobPosting> jobPostings = new ArrayList<>();
}