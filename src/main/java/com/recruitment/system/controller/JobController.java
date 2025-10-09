package com.recruitment.system.controller;

import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.dto.response.PageResponse;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.entity.SavedJob;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.*;
import com.recruitment.system.repository.JobPostingRepository;
import com.recruitment.system.repository.SavedJobRepository;
import com.recruitment.system.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.recruitment.system.config.PaginationValidator;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller xử lý các endpoint public cho job posting
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobPostingRepository jobPostingRepository;
    private final RecommendationService recommendationService;

    private final SavedJobRepository savedJobRepository;


    /**
     * Test endpoint để kiểm tra security
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> testEndpoint() {
        return ResponseEntity.ok(ApiResponse.success("Job controller is working!", "Test successful"));
    }

    /**
     * Tìm kiếm việc làm công khai
     * 
     * @param keyword   Từ khóa tìm kiếm (title, description, skills)
     * @param location  Địa điểm làm việc
     * @param jobType   Loại công việc (FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP)
     * @param minSalary Mức lương tối thiểu
     * @param page      Số trang (mặc định 0)
     * @param size      Kích thước trang (mặc định 10)
     * @param sortBy    Trường sắp xếp (mặc định createdAt)
     * @param sortDir   Hướng sắp xếp (ASC/DESC, mặc định DESC)
     * @return Danh sách việc làm phù hợp
     */
    @GetMapping("/search")
    @PermitAll
    public ResponseEntity<ApiResponse<PageResponse<JobPostingResponse>>> searchJobs(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) String salaryRange,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) CompanySize companySize,
            @RequestParam(required = false) Integer postedWithin,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) String benefits,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        try {
            Pageable pageable = PaginationValidator.buildPageable(
                    page,
                    size,
                    sortBy,
                    sortDir,
                    Set.of("createdAt", "salaryMin", "salaryMax", "applicationDeadline")
            );

            LocalDateTime now = LocalDateTime.now();
            Page<JobPosting> jobPostings;

            // Validation nâng cao
            BigDecimal minRange = null;
            BigDecimal maxRange = null;
            if (salaryRange != null && !salaryRange.isBlank()) {
                if (!salaryRange.matches("\\d+-\\d+")) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("salaryRange không đúng định dạng 'min-max'"));
                }
                String[] parts = salaryRange.split("-");
                try {
                    minRange = new BigDecimal(parts[0]);
                    maxRange = new BigDecimal(parts[1]);
                    if (minRange.compareTo(maxRange) > 0) {
                        return ResponseEntity.badRequest().body(ApiResponse.error("salaryRange: min phải <= max"));
                    }
                } catch (NumberFormatException ex) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("salaryRange phải là số"));
                }
            }

            if (postedWithin != null) {
                if (postedWithin < 1 || postedWithin > 365) {
                    return ResponseEntity.badRequest().body(ApiResponse.error("postedWithin phải trong khoảng 1-365 ngày"));
                }
            }

            if (benefits != null && benefits.length() > 500) {
                return ResponseEntity.badRequest().body(ApiResponse.error("benefits không được vượt quá 500 ký tự"));
            }

            // Chuẩn hóa chuỗi
            String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
            String loc = (location != null && !location.isBlank()) ? location.trim() : null;
            String benefitsFilter = (benefits != null && !benefits.isBlank()) ? benefits.trim().toLowerCase() : null;
            String workModeFilter = workMode != null ? workMode.name().toLowerCase() : null;
            String experienceFilter = experienceLevel != null ? experienceLevel.name().toLowerCase() : null;
            String companySizeFilter = companySize != null ? companySize.name() : null;

            LocalDateTime postedAfter = null;
            if (postedWithin != null) {
                postedAfter = now.minusDays(postedWithin);
            }

            // Gọi truy vấn nâng cao
            jobPostings = jobPostingRepository.searchAdvancedJobs(
                    com.recruitment.system.enums.JobStatus.ACTIVE,
                    now,
                    kw,
                    loc,
                    jobType,
                    minRange != null ? minRange : minSalary,
                    maxRange,
                    postedAfter,
                    experienceFilter,
                    companySizeFilter,
                    workModeFilter,
                    benefitsFilter,
                    pageable
            );

            // Convert to response DTOs
            List<JobPostingResponse> jobResponses = jobPostings.getContent().stream()
                    .map(job -> convertToResponse(job, currentUser))
                    .collect(Collectors.toList());

            // Tạo PageResponse
            PageResponse<JobPostingResponse> pageResponse = new PageResponse<>(
                    jobResponses,
                    jobPostings.getNumber(),
                    jobPostings.getSize(),
                    jobPostings.getTotalElements(),
                    jobPostings.getTotalPages(),
                    jobPostings.isFirst(),
                    jobPostings.isLast(),
                    jobPostings.hasNext(),
                    jobPostings.hasPrevious());

            return ResponseEntity.ok(ApiResponse.success("Tìm kiếm thành công", pageResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi khi tìm kiếm việc làm: " + e.getMessage()));
        }
    }

    /**
     * GET /api/jobs/recommended
     */
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<List<JobPostingResponse>>> getRecommendedJobs(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Chưa xác thực người dùng"));
        }
        if (currentUser.getRole() != UserRole.APPLICANT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Chỉ ứng viên mới được xem gợi ý"));
        }

        // Giới hạn phân trang tối đa 20
        int safeLimit = Math.max(1, Math.min(limit, 20));

        // Kiểm tra độ hoàn thiện profile >= 50% (sử dụng helper trong Profile nếu có); đơn giản: có summary/experience/education/skills
        // Ở đây ủy quyền cho service trả về rỗng nếu chưa đủ
        List<JobPostingResponse> recommended = recommendationService.getRecommendedJobs(currentUser, safeLimit);
        return ResponseEntity.ok(ApiResponse.success("Gợi ý việc làm", recommended));
    }

    /**
     * Lấy chi tiết việc làm công khai
     * 
     * @param id ID của việc làm
     * @return Chi tiết việc làm
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<JobPostingResponse>> getPublicJobDetail(@PathVariable Long id,
                                                                              @AuthenticationPrincipal User currentUser   ) {
        try {
            JobPosting jobPosting = jobPostingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy việc làm"));

            // Kiểm tra việc làm có đang hoạt động không
            if (jobPosting.getStatus() != com.recruitment.system.enums.JobStatus.ACTIVE ||
                    jobPosting.getApplicationDeadline().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Việc làm không còn hoạt động"));
            }

            JobPostingResponse response = convertToResponse(jobPosting, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thành công", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy thông tin việc làm: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách việc làm mới nhất
     * 
     * @param page Số trang
     * @param size Kích thước trang
     * @return Danh sách việc làm mới nhất
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<PageResponse<JobPostingResponse>>> getLatestJobs(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PaginationValidator.buildPageable(
                    page,
                    size,
                    "createdAt",
                    "DESC",
                    Set.of("createdAt")
            );
            LocalDateTime now = LocalDateTime.now();

            Page<JobPosting> jobPostings = jobPostingRepository.findActiveJobs(now, pageable);

            List<JobPostingResponse> jobResponses = jobPostings.getContent().stream()
                    .map(job -> convertToResponse(job, currentUser))
                    .collect(Collectors.toList());

            PageResponse<JobPostingResponse> pageResponse = new PageResponse<>(
                    jobResponses,
                    jobPostings.getNumber(),
                    jobPostings.getSize(),
                    jobPostings.getTotalElements(),
                    jobPostings.getTotalPages(),
                    jobPostings.isFirst(),
                    jobPostings.isLast(),
                    jobPostings.hasNext(),
                    jobPostings.hasPrevious());

            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", pageResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy danh sách việc làm: " + e.getMessage()));
        }
    }

    /**
     * Convert JobPosting entity to JobPostingResponse DTO
     */
    private JobPostingResponse convertToResponse(JobPosting jobPosting) {
        JobPostingResponse response = new JobPostingResponse();
        response.setId(jobPosting.getId());
        response.setTitle(jobPosting.getTitle());
        response.setDescription(jobPosting.getDescription());
        response.setRequirements(jobPosting.getRequirements());
        response.setSkillsRequired(jobPosting.getSkillsRequired());
        response.setLocation(jobPosting.getLocation());
        response.setJobType(jobPosting.getJobType());
        response.setSalaryMin(jobPosting.getSalaryMin());
        response.setSalaryMax(jobPosting.getSalaryMax());
        response.setApplicationDeadline(jobPosting.getApplicationDeadline());
        response.setStatus(jobPosting.getStatus());
        response.setCreatedAt(jobPosting.getCreatedAt());
        response.setUpdatedAt(jobPosting.getUpdatedAt());

        // Set company information
        if (jobPosting.getCompany() != null) {
            com.recruitment.system.entity.Company company = jobPosting.getCompany();
            com.recruitment.system.dto.response.CompanyResponse companyResponse = new com.recruitment.system.dto.response.CompanyResponse();
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

        return response;
    }

    private JobPostingResponse convertToResponse(JobPosting jobPosting, User currentUser) {
        JobPostingResponse response = convertToResponse(jobPosting);

        Optional<SavedJob> savedOpt = savedJobRepository.findByUserIdAndJobPostingId(currentUser.getId(), jobPosting.getId());
        if (savedOpt.isPresent()) {
            response.setIsSaved(true);
            response.setSavedAt(savedOpt.get().getSavedAt());
        } else {
            response.setIsSaved(false);
            response.setSavedAt(null);
        }


        return response;
    }

    /**
     * Lấy chi tiết tin tuyển dụng kèm trạng thái đã lưu (isSaved)
     * Dùng để hiển thị chi tiết tin trong trang “Lưu việc làm yêu thích”
     */
    @GetMapping("/{id}/me")
    @PreAuthorize("hasRole('APPLICANT')")
    @Operation(summary = "Lấy chi tiết tin tuyển dụng kèm trạng thái đã lưu (isSaved)")
    public ResponseEntity<ApiResponse<JobPostingResponse>> getJobDetailWithSavedCheck(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            // Lấy job theo ID
            JobPosting jobPosting = jobPostingRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tin tuyển dụng"));

            // Kiểm tra job còn hoạt động và chưa hết hạn
            if (jobPosting.getStatus() != JobStatus.ACTIVE ||
                    jobPosting.getApplicationDeadline().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Tin tuyển dụng không còn hoạt động"));
            }

            // Convert sang response có cờ isSaved
            JobPostingResponse response = convertToResponse(jobPosting, currentUser);

            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thành công", response));

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }



}
