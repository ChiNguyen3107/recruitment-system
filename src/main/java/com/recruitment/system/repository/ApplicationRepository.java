package com.recruitment.system.repository;

import com.recruitment.system.entity.Application;
import com.recruitment.system.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho Application entity
 */
@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStatus(ApplicationStatus status);

    List<Application> findByApplicantId(Long applicantId);

    List<Application> findByJobPostingId(Long jobPostingId);

    Page<Application> findByApplicantId(Long applicantId, Pageable pageable);

    Page<Application> findByJobPostingId(Long jobPostingId, Pageable pageable);

    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.jobPosting.company.id = :companyId")
    Page<Application> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.jobPosting.company.id = :companyId AND a.status = :status")
    Page<Application> findByCompanyIdAndStatus(@Param("companyId") Long companyId, 
                                              @Param("status") ApplicationStatus status, 
                                              Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.jobPosting.company.id = :companyId AND a.jobPosting.id = :jobPostingId")
    Page<Application> findByCompanyIdAndJobPostingId(@Param("companyId") Long companyId,
                                                    @Param("jobPostingId") Long jobPostingId,
                                                    Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.jobPosting.company.id = :companyId AND a.jobPosting.id = :jobPostingId AND a.status = :status")
    Page<Application> findByCompanyIdAndJobPostingIdAndStatus(@Param("companyId") Long companyId,
                                                             @Param("jobPostingId") Long jobPostingId,
                                                             @Param("status") ApplicationStatus status,
                                                             Pageable pageable);

    Optional<Application> findByApplicantIdAndJobPostingId(Long applicantId, Long jobPostingId);

    boolean existsByApplicantIdAndJobPostingId(Long applicantId, Long jobPostingId);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.status = :status")
    Long countByStatus(@Param("status") ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPosting.company.id = :companyId")
    Long countByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPosting.company.id = :companyId AND a.status = :status")
    Long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.applicant.id = :applicantId")
    Long countByApplicantId(@Param("applicantId") Long applicantId);

    @Query("SELECT a FROM Application a WHERE a.interviewDate BETWEEN :startDate AND :endDate")
    List<Application> findApplicationsWithInterviewBetween(@Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Application a WHERE a.jobPosting.company.id = :companyId AND " +
           "a.interviewDate BETWEEN :startDate AND :endDate")
    List<Application> findCompanyInterviewsBetween(@Param("companyId") Long companyId,
                                                  @Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Application a WHERE a.createdAt >= :startDate")
    Long countApplicationsFromDate(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT a FROM Application a WHERE a.status = 'RECEIVED' AND a.createdAt < :date")
    List<Application> findUnreviewedApplicationsOlderThan(@Param("date") LocalDateTime date);

    // Thống kê theo tháng
    @Query("SELECT MONTH(a.createdAt) as month, COUNT(a) as count FROM Application a " +
           "WHERE YEAR(a.createdAt) = :year GROUP BY MONTH(a.createdAt) ORDER BY month")
    List<Object[]> getApplicationStatsByMonth(@Param("year") int year);

    // Top applicants với nhiều đơn ứng tuyển nhất
    @Query("SELECT a.applicant, COUNT(a) as applicationCount FROM Application a " +
           "GROUP BY a.applicant ORDER BY applicationCount DESC")
    List<Object[]> findTopApplicants(Pageable pageable);
}