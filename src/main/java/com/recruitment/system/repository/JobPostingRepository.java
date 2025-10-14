package com.recruitment.system.repository;

import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho JobPosting entity
 */
@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findByStatus(JobStatus status);

    List<JobPosting> findByJobType(JobType jobType);

    List<JobPosting> findByCompanyId(Long companyId);

    List<JobPosting> findByCreatedById(Long createdById);

    Page<JobPosting> findByStatus(JobStatus status, Pageable pageable);

    Page<JobPosting> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT jp FROM JobPosting jp WHERE jp.company.id = :companyId AND jp.status = :status")
    Page<JobPosting> findByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") JobStatus status, Pageable pageable);

    @Query("SELECT jp FROM JobPosting jp WHERE jp.status = 'ACTIVE' AND jp.applicationDeadline > :now")
    List<JobPosting> findActiveJobs(@Param("now") LocalDateTime now);

    @Query("SELECT jp FROM JobPosting jp WHERE jp.status = 'ACTIVE' AND jp.applicationDeadline > :now")
    Page<JobPosting> findActiveJobs(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT jp FROM JobPosting jp WHERE " +
           "jp.status = 'ACTIVE' AND jp.applicationDeadline > :now AND " +
           "(LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(jp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(jp.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JobPosting> searchActiveJobs(@Param("keyword") String keyword, 
                                     @Param("now") LocalDateTime now, 
                                     Pageable pageable);

    @Query("SELECT jp FROM JobPosting jp WHERE " +
           "jp.status = 'ACTIVE' AND jp.applicationDeadline > :now AND " +
           "LOWER(jp.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<JobPosting> findActiveJobsByLocation(@Param("location") String location, 
                                             @Param("now") LocalDateTime now, 
                                             Pageable pageable);

    @Query("SELECT jp FROM JobPosting jp WHERE " +
           "jp.status = 'ACTIVE' AND jp.applicationDeadline > :now AND " +
           "jp.jobType = :jobType")
    Page<JobPosting> findActiveJobsByType(@Param("jobType") JobType jobType, 
                                         @Param("now") LocalDateTime now, 
                                         Pageable pageable);

    @Query("SELECT jp FROM JobPosting jp WHERE " +
           "jp.status = 'ACTIVE' AND jp.applicationDeadline > :now AND " +
           "((jp.salaryMin IS NOT NULL AND jp.salaryMin >= :minSalary) OR " +
           "(jp.salaryMax IS NOT NULL AND jp.salaryMax >= :minSalary))")
    Page<JobPosting> findActiveJobsBySalary(@Param("minSalary") BigDecimal minSalary, 
                                           @Param("now") LocalDateTime now, 
                                           Pageable pageable);

    @Query("SELECT jp FROM JobPosting jp WHERE " +
           "jp.status = 'ACTIVE' AND jp.applicationDeadline > :now AND " +
           "jp.company.id = :companyId")
    Page<JobPosting> findActiveJobsByCompany(@Param("companyId") Long companyId, 
                                            @Param("now") LocalDateTime now, 
                                            Pageable pageable);

    @Query("SELECT COUNT(jp) FROM JobPosting jp WHERE jp.status = :status")
    Long countByStatus(@Param("status") JobStatus status);

    @Query("SELECT COUNT(jp) FROM JobPosting jp WHERE jp.company.id = :companyId AND jp.status = :status")
    Long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") JobStatus status);

    @Query("SELECT COUNT(jp) FROM JobPosting jp WHERE jp.company.id = :companyId AND jp.status = 'ACTIVE' AND jp.applicationDeadline > :now")
    Long countActiveAndNotExpiredByCompany(@Param("companyId") Long companyId, @Param("now") LocalDateTime now);

    @Query("SELECT jp FROM JobPosting jp WHERE jp.applicationDeadline < :now AND jp.status = 'ACTIVE'")
    List<JobPosting> findExpiredJobs(@Param("now") LocalDateTime now);

    @Query("SELECT jp FROM JobPosting jp ORDER BY jp.viewsCount DESC")
    List<JobPosting> findMostViewedJobs(Pageable pageable);

    @Query("SELECT jp FROM JobPosting jp ORDER BY jp.applicationsCount DESC")
    List<JobPosting> findMostAppliedJobs(Pageable pageable);

    @Query("SELECT jp FROM JobPosting jp WHERE jp.createdAt >= :startDate")
    Long countJobsCreatedFromDate(@Param("startDate") LocalDateTime startDate);

    /**
     * Truy vấn hợp nhất cho tìm kiếm public: chỉ trả job ACTIVE và còn hạn, hỗ trợ keyword/location/jobType/minSalary.
     */
    @Query("SELECT jp FROM JobPosting jp WHERE " +
           "jp.status = 'ACTIVE' AND jp.applicationDeadline > :now AND " +
           "(:location IS NULL OR LOWER(jp.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:jobType IS NULL OR jp.jobType = :jobType) AND " +
           "(:minSalary IS NULL OR ((jp.salaryMin IS NOT NULL AND jp.salaryMin >= :minSalary) OR (jp.salaryMax IS NOT NULL AND jp.salaryMax >= :minSalary))) AND " +
           "(:keyword IS NULL OR (LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(jp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(jp.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<JobPosting> searchPublicJobs(@Param("keyword") String keyword,
                                      @Param("location") String location,
                                      @Param("jobType") JobType jobType,
                                      @Param("minSalary") BigDecimal minSalary,
                                      @Param("now") LocalDateTime now,
                                      Pageable pageable);

    /**
     * Truy vấn nâng cao cho tìm kiếm jobs theo nhiều bộ lọc.
     */
    @Query("SELECT jp FROM JobPosting jp WHERE " +
           "jp.status = :status AND jp.applicationDeadline > :now AND " +
           "(:keyword IS NULL OR (LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(jp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(jp.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%')))) AND " +
           "(:location IS NULL OR LOWER(jp.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:jobType IS NULL OR jp.jobType = :jobType) AND " +
           "(:minSalary IS NULL OR jp.salaryMax >= :minSalary) AND " +
           "(:maxSalary IS NULL OR jp.salaryMin <= :maxSalary) AND " +
           "(:postedAfter IS NULL OR jp.createdAt >= :postedAfter) AND " +
           "(:experienceLevel IS NULL OR (jp.experienceRequired IS NOT NULL AND LOWER(jp.experienceRequired) LIKE LOWER(CONCAT('%', :experienceLevel, '%')))) AND " +
           "(:companySize IS NULL OR (jp.company.companySize IS NOT NULL AND UPPER(jp.company.companySize) = :companySize)) AND " +
           "(:workMode IS NULL OR ( (jp.description IS NOT NULL AND LOWER(jp.description) LIKE LOWER(CONCAT('%', :workMode, '%'))) OR (jp.benefits IS NOT NULL AND LOWER(jp.benefits) LIKE LOWER(CONCAT('%', :workMode, '%'))) )) AND " +
           "(:benefits IS NULL OR (jp.benefits IS NOT NULL AND LOWER(jp.benefits) LIKE LOWER(CONCAT('%', :benefits, '%'))))")
    Page<JobPosting> searchAdvancedJobs(@Param("status") JobStatus status,
                                        @Param("now") LocalDateTime now,
                                        @Param("keyword") String keyword,
                                        @Param("location") String location,
                                        @Param("jobType") JobType jobType,
                                        @Param("minSalary") BigDecimal minSalary,
                                        @Param("maxSalary") BigDecimal maxSalary,
                                        @Param("postedAfter") LocalDateTime postedAfter,
                                        @Param("experienceLevel") String experienceLevel,
                                        @Param("companySize") String companySize,
                                        @Param("workMode") String workMode,
                                        @Param("benefits") String benefits,
                                        Pageable pageable);

    // ===================== Native queries cho admin analytics =====================

    // Jobs by status toàn hệ thống theo tháng hiện tại
    @Query(value = "SELECT jp.status AS status, COUNT(*) AS cnt FROM job_postings jp \n" +
            "WHERE jp.created_at >= DATE_FORMAT(CURDATE(), '%Y-%m-01') GROUP BY jp.status",
            nativeQuery = true)
    List<Object[]> countJobsByStatusThisMonth();

    // Job posting trend 12 tháng gần nhất
    @Query(value = "SELECT YEAR(jp.created_at) AS y, MONTH(jp.created_at) AS m, COUNT(*) AS c \n" +
            "FROM job_postings jp WHERE jp.created_at >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) \n" +
            "GROUP BY y, m ORDER BY y, m", nativeQuery = true)
    List<Object[]> jobPostingTrendLast12Months();

    @Query(value = "SELECT YEAR(jp.created_at) AS y, MONTH(jp.created_at) AS m, COUNT(*) AS c FROM job_postings jp \n" +
            "WHERE (:from IS NULL OR jp.created_at >= :from) AND (:to IS NULL OR jp.created_at <= :to) \n" +
            "GROUP BY y, m ORDER BY y, m", nativeQuery = true)
    List<Object[]> jobPostingTrendInRange(@Param("from") java.time.LocalDateTime from,
                                         @Param("to") java.time.LocalDateTime to);

    // Most popular job types (top 5)
    @Query(value = "SELECT jp.job_type AS jobType, COUNT(*) AS cnt FROM job_postings jp \n" +
            "GROUP BY jp.job_type ORDER BY cnt DESC LIMIT 5", nativeQuery = true)
    List<Object[]> mostPopularJobTypesTop5();

    // Tổng jobs theo trạng thái (group by) toàn hệ thống
    @Query(value = "SELECT jp.status AS status, COUNT(*) AS cnt FROM job_postings jp GROUP BY jp.status", nativeQuery = true)
    List<Object[]> countJobsByStatusGroup();

    // Số job đăng trong tháng hiện tại
    @Query(value = "SELECT COUNT(*) FROM job_postings jp WHERE jp.created_at >= DATE_FORMAT(CURDATE(), '%Y-%m-01')", nativeQuery = true)
    Long countJobsPostedThisMonth();

    // Số công ty hiện có job ACTIVE còn hạn
    @Query(value = "SELECT COUNT(DISTINCT jp.company_id) FROM job_postings jp WHERE jp.status = 'ACTIVE' AND jp.application_deadline > NOW()", nativeQuery = true)
    Long countDistinctCompaniesWithActiveJobs();

    //Danh sách các tin tuyển dụng (JobPosting) có thời hạn nộp hồ sơ (applicationDeadline) nằm trong khoảng từ ngày from đến ngày to
    List<JobPosting> findByApplicationDeadlineBetween(LocalDateTime from, LocalDateTime to);

}