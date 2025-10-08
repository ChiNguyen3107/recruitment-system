package com.recruitment.system.entity;

import com.recruitment.system.enums.InterviewStatus;
import com.recruitment.system.enums.InterviewType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Lịch phỏng vấn cho đơn ứng tuyển
 */
@Entity
@Table(name = "interview_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false, unique = true)
    private Long applicationId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 60;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "address", length = 255)
    private String location;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink;

    @Column(name = "method", nullable = false)
    private String method; // ONLINE | OFFLINE

    @Column(name = "note", length = 500)
    private String notes;

    @Column(name = "status", nullable = false)
    private String dbStatus = "MOI_TAO"; // MOI_TAO | XAC_NHAN | HOAN_TAT | HUY

    @Column(name = "responsible_user_id", nullable = false)
    private Long scheduledBy;

    @Transient
    private LocalDateTime createdAt;

    @Transient
    private LocalDateTime updatedAt;
}


