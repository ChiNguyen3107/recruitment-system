package com.recruitment.system.entity;

import com.recruitment.system.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho đơn ứng tuyển
 */
@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.RECEIVED;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(name = "additional_documents")
    private String additionalDocuments;

    @Column(name = "interview_date")
    private LocalDateTime interviewDate;

    @Column(name = "interview_location")
    private String interviewLocation;

    @Column(name = "interview_notes", columnDefinition = "TEXT")
    private String interviewNotes;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "offer_details", columnDefinition = "TEXT")
    private String offerDetails;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Quan hệ với JobPosting
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    // Quan hệ với User (applicant)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    // Helper methods
    public boolean isReviewed() {
        return status != ApplicationStatus.RECEIVED;
    }

    public boolean isInProgress() {
        return status == ApplicationStatus.RECEIVED || 
               status == ApplicationStatus.REVIEWED || 
               status == ApplicationStatus.INTERVIEW;
    }

    public boolean isCompleted() {
        return status == ApplicationStatus.HIRED || 
               status == ApplicationStatus.REJECTED;
    }

    public boolean canScheduleInterview() {
        return status == ApplicationStatus.REVIEWED;
    }

    public boolean canMakeOffer() {
        return status == ApplicationStatus.INTERVIEW;
    }

    public void updateStatus(ApplicationStatus newStatus, String notes) {
        this.status = newStatus;
        this.reviewedAt = LocalDateTime.now();
        
        switch (newStatus) {
            case REJECTED:
                this.rejectionReason = notes;
                break;
            case OFFER:
                this.offerDetails = notes;
                break;
            case INTERVIEW:
                this.interviewNotes = notes;
                break;
            default:
                this.feedback = notes;
                break;
        }
    }
}