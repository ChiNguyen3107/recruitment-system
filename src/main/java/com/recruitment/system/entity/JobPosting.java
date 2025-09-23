package com.recruitment.system.entity;

import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.enums.JobType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho tin tuyển dụng
 */
@Entity
@Table(name = "job_postings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.DRAFT;

    private String location;

    @Column(name = "salary_min")
    private BigDecimal salaryMin;

    @Column(name = "salary_max")
    private BigDecimal salaryMax;

    @Column(name = "salary_currency")
    private String salaryCurrency = "VND";

    @Column(name = "experience_required")
    private String experienceRequired;

    @Column(name = "education_required")
    private String educationRequired;

    @Column(name = "skills_required")
    private String skillsRequired;

    @Column(name = "number_of_positions")
    private Integer numberOfPositions = 1;

    @Column(name = "application_deadline")
    private LocalDateTime applicationDeadline;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "views_count")
    private Integer viewsCount = 0;

    @Column(name = "applications_count")
    private Integer applicationsCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Quan hệ với Company
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // Quan hệ với User (người tạo tin)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // Quan hệ với Applications
    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Application> applications = new ArrayList<>();

    // Helper methods
    public boolean isActive() {
        return status == JobStatus.ACTIVE && 
               applicationDeadline != null && 
               applicationDeadline.isAfter(LocalDateTime.now());
    }

    public boolean canReceiveApplications() {
        return isActive();
    }

    public void incrementViewsCount() {
        this.viewsCount = this.viewsCount == null ? 1 : this.viewsCount + 1;
    }

    public void incrementApplicationsCount() {
        this.applicationsCount = this.applicationsCount == null ? 1 : this.applicationsCount + 1;
    }
}