package com.recruitment.system.repository;

import com.recruitment.system.entity.Company;
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
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho JobPosting entity
 */
@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findByStatus(JobStatus status);
    Page<JobPosting> findByStatus(JobStatus status, Pageable pageable);

    List<JobPosting> findByJobType(JobType jobType);

    List<JobPosting> findByCompanyId(Long companyId);
    
    Page<JobPosting> findByCompany(Company company, Pageable pageable);

    List<JobPosting> findByCreatedById(Long createdById);
    
    Page<JobPosting> findByTitleContainingIgnoreCaseAndStatus(String title, JobStatus status, Pageable pageable);

    Page<JobPosting> findByCompanyId(Long companyId, Pageable pageable);

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

    @Query("SELECT jp FROM JobPosting jp WHERE jp.applicationDeadline < :now AND jp.status = 'ACTIVE'")
    List<JobPosting> findExpiredJobs(@Param("now") LocalDateTime now);

    @Query("SELECT jp FROM JobPosting jp ORDER BY jp.viewsCount DESC")
    List<JobPosting> findMostViewedJobs(Pageable pageable);

    @Query("SELECT jp FROM JobPosting jp ORDER BY jp.applicationsCount DESC")
    List<JobPosting> findMostAppliedJobs(Pageable pageable);

    @Query("SELECT jp FROM JobPosting jp WHERE jp.createdAt >= :startDate")
    Long countJobsCreatedFromDate(@Param("startDate") LocalDateTime startDate);
}