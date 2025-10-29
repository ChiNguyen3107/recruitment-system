package com.recruitment.system.controller;

import com.recruitment.system.config.AuditLogger;
import com.recruitment.system.dto.request.JobPostingRequest;
import com.recruitment.system.dto.request.JobStatusRequest;
import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.CompanyResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.dto.response.UserResponse;
import com.recruitment.system.entity.Company;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.repository.JobPostingRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.recruitment.system.config.PaginationValidator;
import com.recruitment.system.dto.response.PageResponse;
import java.util.Set;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller quản lý CRUD tin tuyển dụng cho EMPLOYER/RECRUITER/ADMIN
 */
@RestController
@RequestMapping("/api/jobs/manage")
@RequiredArgsConstructor
public class ManageJobController {

    private final JobPostingRepository jobPostingRepository;
    private final AuditLogger auditLogger;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobPostingResponse>>> listJobs(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) JobStatus status
    ) {
        try {
            validateEmployerContext(currentUser);

            Pageable pageable = PaginationValidator.buildPageable(
                    page,
                    size,
                    sortBy,
                    sortDir,
                    Set.of("createdAt", "title", "status", "applicationDeadline")
            );


            Page<JobPosting> jobs;


            if (currentUser.isAdmin()) {
                if (status != null) {
                    jobs = jobPostingRepository.findByStatus(status, pageable);
                } else {
                    jobs = jobPostingRepository.findAll(pageable);
                }
            }
            // ✅ Employer chỉ xem job thuộc công ty họ
            
            else {
                if (currentUser.getCompany() == null) {
                    throw new RuntimeException("Tài khoản chưa liên kết công ty");
                }

                Long companyId = currentUser.getCompany().getId();

                if (status != null) {
                    jobs = jobPostingRepository.findByCompanyIdAndStatus(companyId, status, pageable);
                } else {
                    jobs = jobPostingRepository.findByCompanyId(companyId, pageable);
                }
            }

            List<JobPostingResponse> jobResponses = jobs.getContent().stream()
                    .map(this::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());

            PageResponse<JobPostingResponse> pageResponse = new PageResponse<>(
                    jobResponses,
                    jobs.getNumber(),
                    jobs.getSize(),
                    jobs.getTotalElements(),
                    jobs.getTotalPages(),
                    jobs.isFirst(),
                    jobs.isLast(),
                    jobs.hasNext(),
                    jobs.hasPrevious()
            );

            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách jobs thành công", pageResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy danh sách jobs: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobPostingResponse>> createJob(
            @Valid @RequestBody JobPostingRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest
    ) {
        try {
            validateEmployerContext(currentUser);
            validateDeadline(request.getApplicationDeadline());

            JobPosting job = new JobPosting();
            applyRequestToEntity(job, request);

            // Bind createdBy và company từ user hiện tại
            job.setCreatedBy(currentUser);
            job.setCompany(currentUser.getCompany());

            if (job.getStatus() == JobStatus.ACTIVE) {
                ensureActiveConstraints(job);
                job.setPublishedAt(LocalDateTime.now());
            }

            JobPosting saved = jobPostingRepository.save(job);

            String clientIp = getClientIpAddress(httpRequest);
            auditLogger.logJobCreated(
                    saved.getId(),
                    currentUser.getCompany() != null ? currentUser.getCompany().getId() : null,
                    currentUser.getEmail(),
                    clientIp,
                    httpRequest.getHeader("User-Agent")
            );
            return ResponseEntity.ok(ApiResponse.success("Tạo tin thành công", convertToResponse(saved)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi tạo tin: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobPostingResponse>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobPostingRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest
    ) {
        try {
            validateEmployerContext(currentUser);
            validateDeadline(request.getApplicationDeadline());

            JobPosting job = jobPostingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng"));

            enforceOwnershipOrAdmin(job, currentUser);

            applyRequestToEntity(job, request);

            // Không cho đổi company/createdBy bằng payload
            job.setCompany(currentUser.getCompany());
            job.setCreatedBy(job.getCreatedBy() == null ? currentUser : job.getCreatedBy());

            if (job.getStatus() == JobStatus.ACTIVE) {
                ensureActiveConstraints(job);
                if (job.getPublishedAt() == null) {
                    job.setPublishedAt(LocalDateTime.now());
                }
            }

            JobPosting saved = jobPostingRepository.save(job);

            String clientIp = getClientIpAddress(httpRequest);
            auditLogger.logJobUpdated(
                    saved.getId(),
                    currentUser.getCompany() != null ? currentUser.getCompany().getId() : null,
                    currentUser.getEmail(),
                    clientIp,
                    httpRequest.getHeader("User-Agent")
            );
            return ResponseEntity.ok(ApiResponse.success("Cập nhật tin thành công", convertToResponse(saved)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi cập nhật tin: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteJob(
            @PathVariable Long id,
            @RequestParam(name = "hard", defaultValue = "false") boolean hard,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest
    ) {
        try {
            validateEmployerContext(currentUser);
            JobPosting job = jobPostingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng"));

            enforceOwnershipOrAdmin(job, currentUser);

            if (hard) {
                jobPostingRepository.delete(job);
                String clientIp = getClientIpAddress(httpRequest);
                auditLogger.logJobDeleted(
                        job.getId(),
                        job.getCompany() != null ? job.getCompany().getId() : null,
                        currentUser.getEmail(),
                        true,
                        clientIp,
                        httpRequest.getHeader("User-Agent")
                );
                return ResponseEntity.ok(ApiResponse.success("Xóa vĩnh viễn thành công", "deleted"));
            } else {
                job.setStatus(JobStatus.CLOSED);
                jobPostingRepository.save(job);
                String clientIp = getClientIpAddress(httpRequest);
                auditLogger.logJobDeleted(
                        job.getId(),
                        job.getCompany() != null ? job.getCompany().getId() : null,
                        currentUser.getEmail(),
                        false,
                        clientIp,
                        httpRequest.getHeader("User-Agent")
                );
                return ResponseEntity.ok(ApiResponse.success("Đã đóng (xóa mềm) tin tuyển dụng", "soft_deleted"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi xóa tin: " + e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<JobPostingResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody JobStatusRequest request,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest
    ) {
        try {
            validateEmployerContext(currentUser);
            JobPosting job = jobPostingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng"));

            enforceOwnershipOrAdmin(job, currentUser);

            JobStatus oldStatus = job.getStatus();
            JobStatus newStatus = request.getStatus();
            if (newStatus == JobStatus.ACTIVE) {
                ensureActiveConstraints(job);
                if (job.getPublishedAt() == null) {
                    job.setPublishedAt(LocalDateTime.now());
                }
            }
            job.setStatus(newStatus);

            JobPosting saved = jobPostingRepository.save(job);

            String clientIp = getClientIpAddress(httpRequest);
            auditLogger.logJobStatusChanged(
                    saved.getId(),
                    currentUser.getCompany() != null ? currentUser.getCompany().getId() : null,
                    oldStatus != null ? oldStatus.name() : null,
                    newStatus != null ? newStatus.name() : null,
                    currentUser.getEmail(),
                    clientIp,
                    httpRequest.getHeader("User-Agent")
            );
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", convertToResponse(saved)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi cập nhật trạng thái: " + e.getMessage()));
        }
    }

    private void applyRequestToEntity(JobPosting job, JobPostingRequest req) {
        job.setTitle(req.getTitle());
        job.setDescription(req.getDescription());
        job.setRequirements(req.getRequirements());
        job.setBenefits(req.getBenefits());
        job.setJobType(req.getJobType());
        job.setStatus(req.getStatus() == null ? JobStatus.DRAFT : req.getStatus());
        job.setLocation(req.getLocation());
        job.setSalaryMin(req.getSalaryMin());
        job.setSalaryMax(req.getSalaryMax());
        job.setSalaryCurrency(req.getSalaryCurrency());
        job.setExperienceRequired(req.getExperienceRequired());
        job.setEducationRequired(req.getEducationRequired());
        job.setSkillsRequired(req.getSkillsRequired());
        job.setNumberOfPositions(req.getNumberOfPositions());
        job.setApplicationDeadline(req.getApplicationDeadline());
    }

    private void validateEmployerContext(User currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }
        if (currentUser.getCompany() == null && !currentUser.isAdmin()) {
            throw new RuntimeException("Tài khoản chưa liên kết công ty");
        }
    }

    private void validateDeadline(LocalDateTime deadline) {
        if (deadline == null) {
            throw new RuntimeException("Hạn nộp đơn không được để trống");
        }
        if (!deadline.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Hạn nộp đơn phải lớn hơn thời điểm hiện tại");
        }
    }

    private void ensureActiveConstraints(JobPosting job) {
        if (job.getApplicationDeadline() == null || !job.getApplicationDeadline().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Khi bật ACTIVE, hạn nộp đơn phải ở tương lai");
        }
    }

    private void enforceOwnershipOrAdmin(JobPosting job, User currentUser) {
        if (currentUser.isAdmin()) return;
        Company company = currentUser.getCompany();
        if (company == null || job.getCompany() == null || !company.getId().equals(job.getCompany().getId())) {
            throw new RuntimeException("Bạn không có quyền thao tác tin này");
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    private JobPostingResponse convertToResponse(JobPosting jobPosting) {
        JobPostingResponse response = new JobPostingResponse();
        response.setId(jobPosting.getId());
        response.setTitle(jobPosting.getTitle());
        response.setDescription(jobPosting.getDescription());
        response.setRequirements(jobPosting.getRequirements());
        response.setBenefits(jobPosting.getBenefits());
        response.setJobType(jobPosting.getJobType());
        response.setStatus(jobPosting.getStatus());
        response.setLocation(jobPosting.getLocation());
        response.setSalaryMin(jobPosting.getSalaryMin());
        response.setSalaryMax(jobPosting.getSalaryMax());
        response.setSalaryCurrency(jobPosting.getSalaryCurrency());
        response.setExperienceRequired(jobPosting.getExperienceRequired());
        response.setEducationRequired(jobPosting.getEducationRequired());
        response.setSkillsRequired(jobPosting.getSkillsRequired());
        response.setNumberOfPositions(jobPosting.getNumberOfPositions());
        response.setApplicationDeadline(jobPosting.getApplicationDeadline());
        response.setPublishedAt(jobPosting.getPublishedAt());
        response.setViewsCount(jobPosting.getViewsCount());
        response.setApplicationsCount(jobPosting.getApplicationsCount());
        response.setCreatedAt(jobPosting.getCreatedAt());
        response.setUpdatedAt(jobPosting.getUpdatedAt());

        if (jobPosting.getCompany() != null) {
            Company company = jobPosting.getCompany();
            CompanyResponse companyResponse = new CompanyResponse();
            companyResponse.setId(company.getId());
            companyResponse.setName(company.getName());
            companyResponse.setDescription(company.getDescription());
            companyResponse.setWebsite(company.getWebsite());
            companyResponse.setIndustry(company.getIndustry());
            companyResponse.setCompanySize(company.getCompanySize());
            companyResponse.setAddress(company.getAddress());
            companyResponse.setCity(company.getCity());
            companyResponse.setCountry(company.getCountry());
            companyResponse.setPhoneNumber(company.getPhoneNumber());
            companyResponse.setContactEmail(company.getContactEmail());
            companyResponse.setLogoUrl(company.getLogoUrl());
            companyResponse.setIsVerified(company.getIsVerified());
            companyResponse.setCreatedAt(company.getCreatedAt());
            response.setCompany(companyResponse);
        }

        if (jobPosting.getCreatedBy() != null) {
            User creator = jobPosting.getCreatedBy();
            UserResponse userResponse = new UserResponse();
            userResponse.setId(creator.getId());
            userResponse.setEmail(creator.getEmail());
            userResponse.setFirstName(creator.getFirstName());
            userResponse.setLastName(creator.getLastName());
            userResponse.setFullName(creator.getFullName());
            userResponse.setPhoneNumber(creator.getPhoneNumber());
            userResponse.setRole(creator.getRole());
            userResponse.setStatus(creator.getStatus());
            userResponse.setEmailVerified(creator.getEmailVerified());
            userResponse.setAvatarUrl(creator.getAvatarUrl());
            userResponse.setLastLogin(creator.getLastLogin());
            userResponse.setCreatedAt(creator.getCreatedAt());
            response.setCreatedBy(userResponse);
        }

        boolean canApply = jobPosting.getStatus() == JobStatus.ACTIVE
                && jobPosting.getApplicationDeadline() != null
                && jobPosting.getApplicationDeadline().isAfter(LocalDateTime.now());
        response.setCanApply(canApply);
        response.setIsExpired(jobPosting.getApplicationDeadline() != null && jobPosting.getApplicationDeadline().isBefore(LocalDateTime.now()));

        return response;
    }
}


