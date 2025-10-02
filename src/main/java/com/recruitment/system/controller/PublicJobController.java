package com.recruitment.system.controller;

import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.dto.response.PageResponse;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.enums.JobType;
import com.recruitment.system.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.recruitment.system.config.PaginationValidator;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller xử lý các endpoint public cho job posting
 */
@RestController
@RequestMapping("/api/public/jobs")
@RequiredArgsConstructor
public class PublicJobController {

    private final JobPostingRepository jobPostingRepository;

    /**
     * Tìm kiếm việc làm công khai
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<JobPostingResponse>>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) BigDecimal minSalary,
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
                    Set.of("createdAt", "publishedAt", "salaryMax")
            );

            LocalDateTime now = LocalDateTime.now();
            String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
            String loc = (location != null && !location.trim().isEmpty()) ? location.trim() : null;
            Page<JobPosting> jobPostings = jobPostingRepository.searchPublicJobs(
                    kw,
                    loc,
                    jobType,
                    minSalary,
                    now,
                    pageable
            );

            // Convert to response DTOs
            List<JobPostingResponse> jobResponses = jobPostings.getContent().stream()
                    .map(this::convertToResponse)
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
     * Lấy chi tiết việc làm công khai
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobPostingResponse>> getPublicJobDetail(@PathVariable Long id) {
        try {
            JobPosting jobPosting = jobPostingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy việc làm"));

            // Kiểm tra việc làm có đang hoạt động không
            if (jobPosting.getStatus() != com.recruitment.system.enums.JobStatus.ACTIVE ||
                    jobPosting.getApplicationDeadline().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Việc làm không còn hoạt động"));
            }

            JobPostingResponse response = convertToResponse(jobPosting);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thành công", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy thông tin việc làm: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách việc làm mới nhất
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<PageResponse<JobPostingResponse>>> getLatestJobs(
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
                    .map(this::convertToResponse)
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
}
