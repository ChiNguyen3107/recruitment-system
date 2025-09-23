package com.recruitment.system.controller;

import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.dto.response.PageResponse;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.enums.JobType;
import com.recruitment.system.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        try {
            // Tạo Pageable object
            Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            LocalDateTime now = LocalDateTime.now();
            Page<JobPosting> jobPostings;

            // Tìm kiếm dựa trên các tham số
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Tìm kiếm theo từ khóa
                jobPostings = jobPostingRepository.searchActiveJobs(keyword.trim(), now, pageable);
            } else if (location != null && !location.trim().isEmpty()) {
                // Tìm kiếm theo địa điểm
                jobPostings = jobPostingRepository.findActiveJobsByLocation(location.trim(), now, pageable);
            } else if (jobType != null) {
                // Tìm kiếm theo loại công việc
                jobPostings = jobPostingRepository.findActiveJobsByType(jobType, now, pageable);
            } else if (minSalary != null) {
                // Tìm kiếm theo mức lương
                jobPostings = jobPostingRepository.findActiveJobsBySalary(minSalary, now, pageable);
            } else {
                // Lấy tất cả việc làm đang hoạt động
                jobPostings = jobPostingRepository.findActiveJobs(now, pageable);
            }

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
     * 
     * @param id ID của việc làm
     * @return Chi tiết việc làm
     */
    @GetMapping("/public/{id}")
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
     * 
     * @param page Số trang
     * @param size Kích thước trang
     * @return Danh sách việc làm mới nhất
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<PageResponse<JobPostingResponse>>> getLatestJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
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
