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

    @Query("SELECT a FROM Application a WHERE a.applicant.id = :applicantId AND a.status = :status")
    Page<Application> findByApplicantIdAndStatus(@Param("applicantId") Long applicantId, @Param("status") ApplicationStatus status, Pageable pageable);

    Page<Application> findByJobPostingId(Long jobPostingId, Pageable pageable);

    Page<Application> findByStatus(ApplicationStatus status, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.jobPosting.company.id = :companyId")
    Page<Application> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT a FROM Application a WHERE a.jobPosting.company.id = :companyId AND a.status = :status")
    Page<Application> findByCompanyIdAndStatus(@Param("companyId") Long companyId, 
                                              @Param("status") ApplicationStatus status, 
                                              Pageable pageable);


    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPosting.company.id = :companyId AND a.status = 'HIRED'")
    Long countHiredByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT AVG(TIMESTAMPDIFF(DAY, a.createdAt, a.reviewedAt)) FROM Application a WHERE a.jobPosting.company.id = :companyId AND a.reviewedAt IS NOT NULL")
    Double averageResponseDaysByCompany(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPosting.company.id = :companyId")
    Long countByCompanyId(@Param("companyId") Long companyId);

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

    // giữ lại một phiên bản duy nhất của countByCompanyId

    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPosting.company.id = :companyId AND a.status = :status")
    Long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.applicant.id = :applicantId")
    Long countByApplicantId(@Param("applicantId") Long applicantId);

    Long countByJobPostingId(Long jobPostingId);

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

    // ===================== Native queries cho analytics nhanh =====================

    // 1) Applications by status cho một company
    @Query(value = "SELECT a.status AS status, COUNT(*) AS cnt FROM applications a \n" +
            "JOIN job_postings jp ON jp.id = a.job_posting_id \n" +
            "WHERE jp.company_id = :companyId GROUP BY a.status", nativeQuery = true)
    List<Object[]> countApplicationsByStatusForCompany(@Param("companyId") Long companyId);

    @Query(value = "SELECT a.status AS status, COUNT(*) AS cnt FROM applications a \n" +
            "JOIN job_postings jp ON jp.id = a.job_posting_id \n" +
            "WHERE jp.company_id = :companyId \n" +
            "AND (:from IS NULL OR a.created_at >= :from) \n" +
            "AND (:to IS NULL OR a.created_at <= :to) \n" +
            "GROUP BY a.status",
            nativeQuery = true)
    List<Object[]> countApplicationsByStatusForCompanyInRange(@Param("companyId") Long companyId,
                                                             @Param("from") java.time.LocalDateTime from,
                                                             @Param("to") java.time.LocalDateTime to);

    // 2) Top performing jobs theo số applications (top N)
    @Query(value = "SELECT jp.title AS jobTitle, COUNT(a.id) AS appCount, \n" +
            "SUM(CASE WHEN a.status = 'INTERVIEW' THEN 1 ELSE 0 END) AS interviewCount, \n" +
            "SUM(CASE WHEN a.status = 'HIRED' THEN 1 ELSE 0 END) AS hiredCount \n" +
            "FROM job_postings jp LEFT JOIN applications a ON a.job_posting_id = jp.id \n" +
            "WHERE jp.company_id = :companyId GROUP BY jp.id, jp.title \n" +
            "ORDER BY appCount DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopJobsByApplicationsForCompany(@Param("companyId") Long companyId, @Param("limit") int limit);

    @Query(value = "SELECT jp.title AS jobTitle, COUNT(a.id) AS appCount, \n" +
            "SUM(CASE WHEN a.status = 'INTERVIEW' THEN 1 ELSE 0 END) AS interviewCount, \n" +
            "SUM(CASE WHEN a.status = 'HIRED' THEN 1 ELSE 0 END) AS hiredCount \n" +
            "FROM job_postings jp LEFT JOIN applications a ON a.job_posting_id = jp.id \n" +
            "WHERE jp.company_id = :companyId \n" +
            "AND (:from IS NULL OR a.created_at >= :from) \n" +
            "AND (:to IS NULL OR a.created_at <= :to) \n" +
            "GROUP BY jp.id, jp.title \n" +
            "ORDER BY appCount DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopJobsByApplicationsForCompanyInRange(@Param("companyId") Long companyId,
                                                             @Param("limit") int limit,
                                                             @Param("from") java.time.LocalDateTime from,
                                                             @Param("to") java.time.LocalDateTime to);

    // 3) Average time to hire (RECEIVED -> HIRED) tính theo ngày dựa trên application_timelines
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(DAY, t_received.changed_at, t_hired.changed_at)) AS avg_days \n" +
            "FROM application_timelines t_received \n" +
            "JOIN application_timelines t_hired ON t_hired.application_id = t_received.application_id \n" +
            "JOIN applications a ON a.id = t_received.application_id \n" +
            "JOIN job_postings jp ON jp.id = a.job_posting_id \n" +
            "WHERE jp.company_id = :companyId AND t_received.to_status = 'RECEIVED' AND t_hired.to_status = 'HIRED'\n",
            nativeQuery = true)
    Double averageDaysFromReceivedToHired(@Param("companyId") Long companyId);

    @Query(value = "SELECT AVG(TIMESTAMPDIFF(DAY, t_received.changed_at, t_hired.changed_at)) AS avg_days \n" +
            "FROM application_timelines t_received \n" +
            "JOIN application_timelines t_hired ON t_hired.application_id = t_received.application_id \n" +
            "JOIN applications a ON a.id = t_received.application_id \n" +
            "JOIN job_postings jp ON jp.id = a.job_posting_id \n" +
            "WHERE jp.company_id = :companyId AND t_received.to_status = 'RECEIVED' AND t_hired.to_status = 'HIRED' \n" +
            "AND (:from IS NULL OR t_received.changed_at >= :from) \n" +
            "AND (:to IS NULL OR t_hired.changed_at <= :to)",
            nativeQuery = true)
    Double averageDaysFromReceivedToHiredInRange(@Param("companyId") Long companyId,
                                                @Param("from") java.time.LocalDateTime from,
                                                @Param("to") java.time.LocalDateTime to);

    // 4) Conversion rates theo giai đoạn cho company dựa vào timelines
    @Query(value = "SELECT \n" +
            "SUM(CASE WHEN t.to_status = 'RECEIVED' THEN 1 ELSE 0 END) AS received,\n" +
            "SUM(CASE WHEN t.to_status = 'REVIEWED' THEN 1 ELSE 0 END) AS reviewed,\n" +
            "SUM(CASE WHEN t.to_status = 'INTERVIEW' THEN 1 ELSE 0 END) AS interviewed,\n" +
            "SUM(CASE WHEN t.to_status = 'OFFER' THEN 1 ELSE 0 END) AS offered,\n" +
            "SUM(CASE WHEN t.to_status = 'HIRED' THEN 1 ELSE 0 END) AS hired\n" +
            "FROM application_timelines t \n" +
            "JOIN applications a ON a.id = t.application_id \n" +
            "JOIN job_postings jp ON jp.id = a.job_posting_id \n" +
            "WHERE jp.company_id = :companyId",
            nativeQuery = true)
    Object[] countStageTotalsForCompany(@Param("companyId") Long companyId);

    @Query(value = "SELECT \n" +
            "SUM(CASE WHEN t.to_status = 'RECEIVED' THEN 1 ELSE 0 END) AS received,\n" +
            "SUM(CASE WHEN t.to_status = 'REVIEWED' THEN 1 ELSE 0 END) AS reviewed,\n" +
            "SUM(CASE WHEN t.to_status = 'INTERVIEW' THEN 1 ELSE 0 END) AS interviewed,\n" +
            "SUM(CASE WHEN t.to_status = 'OFFER' THEN 1 ELSE 0 END) AS offered,\n" +
            "SUM(CASE WHEN t.to_status = 'HIRED' THEN 1 ELSE 0 END) AS hired\n" +
            "FROM application_timelines t \n" +
            "JOIN applications a ON a.id = t.application_id \n" +
            "JOIN job_postings jp ON jp.id = a.job_posting_id \n" +
            "WHERE jp.company_id = :companyId \n" +
            "AND (:from IS NULL OR t.changed_at >= :from) \n" +
            "AND (:to IS NULL OR t.changed_at <= :to)",
            nativeQuery = true)
    Object[] countStageTotalsForCompanyInRange(@Param("companyId") Long companyId,
                                              @Param("from") java.time.LocalDateTime from,
                                              @Param("to") java.time.LocalDateTime to);

    // 5) Hiring trend 12 tháng gần nhất (HIRED theo tháng) cho company
    @Query(value = "SELECT YEAR(t.changed_at) AS y, MONTH(t.changed_at) AS m, COUNT(*) AS c \n" +
            "FROM application_timelines t \n" +
            "JOIN applications a ON a.id = t.application_id \n" +
            "JOIN job_postings jp ON jp.id = a.job_posting_id \n" +
            "WHERE jp.company_id = :companyId AND t.to_status = 'HIRED' AND t.changed_at >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) \n" +
            "GROUP BY y, m ORDER BY y, m",
            nativeQuery = true)
    List<Object[]> hiringTrendLast12MonthsForCompany(@Param("companyId") Long companyId);

    @Query(value = "SELECT YEAR(t.changed_at) AS y, MONTH(t.changed_at) AS m, COUNT(*) AS c \n" +
            "FROM application_timelines t \n" +
            "JOIN applications a ON a.id = t.application_id \n" +
            "JOIN job_postings jp ON jp.id = a.job_posting_id \n" +
            "WHERE jp.company_id = :companyId AND t.to_status = 'HIRED' \n" +
            "AND (:from IS NULL OR t.changed_at >= :from) \n" +
            "AND (:to IS NULL OR t.changed_at <= :to) \n" +
            "GROUP BY y, m ORDER BY y, m",
            nativeQuery = true)
    List<Object[]> hiringTrendForCompanyInRange(@Param("companyId") Long companyId,
                                               @Param("from") java.time.LocalDateTime from,
                                               @Param("to") java.time.LocalDateTime to);

    // ===================== Admin metrics =====================

    // Applications by status toàn hệ thống, tháng hiện tại
    @Query(value = "SELECT a.status AS status, COUNT(*) AS cnt FROM applications a \n" +
            "WHERE a.created_at >= DATE_FORMAT(CURDATE(), '%Y-%m-01') GROUP BY a.status",
            nativeQuery = true)
    List<Object[]> countApplicationsByStatusThisMonth();

    @Query(value = "SELECT a.status AS status, COUNT(*) AS cnt FROM applications a \n" +
            "WHERE (:from IS NULL OR a.created_at >= :from) AND (:to IS NULL OR a.created_at <= :to) GROUP BY a.status",
            nativeQuery = true)
    List<Object[]> countApplicationsByStatusInRange(@Param("from") java.time.LocalDateTime from,
                                                   @Param("to") java.time.LocalDateTime to);

    // Application volume trend 12 tháng gần nhất
    @Query(value = "SELECT YEAR(a.created_at) AS y, MONTH(a.created_at) AS m, COUNT(*) AS c \n" +
            "FROM applications a WHERE a.created_at >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) \n" +
            "GROUP BY y, m ORDER BY y, m", nativeQuery = true)
    List<Object[]> applicationVolumeTrendLast12Months();

    @Query(value = "SELECT YEAR(a.created_at) AS y, MONTH(a.created_at) AS m, COUNT(*) AS c FROM applications a \n" +
            "WHERE (:from IS NULL OR a.created_at >= :from) AND (:to IS NULL OR a.created_at <= :to) \n" +
            "GROUP BY y, m ORDER BY y, m",
            nativeQuery = true)
    List<Object[]> applicationVolumeTrendInRange(@Param("from") java.time.LocalDateTime from,
                                                @Param("to") java.time.LocalDateTime to);

    // Tổng applications theo trạng thái (group by) toàn hệ thống
    @Query(value = "SELECT a.status AS status, COUNT(*) AS cnt FROM applications a GROUP BY a.status", nativeQuery = true)
    List<Object[]> countApplicationsByStatusGroup();

    // Số application tạo trong tháng hiện tại
    @Query(value = "SELECT COUNT(*) FROM applications a WHERE a.created_at >= DATE_FORMAT(CURDATE(), '%Y-%m-01')", nativeQuery = true)
    Long countApplicationsThisMonth();

    // Most active companies (top N by applications received)
    @Query(value = "SELECT jp.company_id AS companyId, c.name AS companyName, COUNT(a.id) AS cnt \n" +
            "FROM applications a \n" +
            "JOIN job_postings jp ON jp.id = a.job_posting_id \n" +
            "JOIN companies c ON c.id = jp.company_id \n" +
            "GROUP BY jp.company_id, c.name ORDER BY cnt DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> mostActiveCompanies(@Param("limit") int limit);
}