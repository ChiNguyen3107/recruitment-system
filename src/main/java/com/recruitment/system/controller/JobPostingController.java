package com.recruitment.system.controller;

import com.recruitment.system.dto.request.JobPostingRequest;
import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.dto.response.PageResponse;
import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.service.JobPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý quản lý tin tuyển dụng
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    /**
     * Tạo tin tuyển dụng mới (Employer only)
     */
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<JobPostingResponse>> createJobPosting(
            @Valid @RequestBody JobPostingRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            JobPostingResponse jobPosting = jobPostingService.createJobPosting(email, request);
            return ResponseEntity.ok(ApiResponse.success("Tạo tin tuyển dụng thành công", jobPosting));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cập nhật tin tuyển dụng (Employer only)
     */
    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobPostingResponse>> updateJobPosting(
            @PathVariable Long jobId,
            @Valid @RequestBody JobPostingRequest request,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            JobPostingResponse jobPosting = jobPostingService.updateJobPosting(email, jobId, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật tin tuyển dụng thành công", jobPosting));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa tin tuyển dụng (Employer only)
     */
    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteJobPosting(
            @PathVariable Long jobId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            jobPostingService.deleteJobPosting(email, jobId);
            return ResponseEntity.ok(ApiResponse.success("Xóa tin tuyển dụng thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy chi tiết tin tuyển dụng (Public)
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobPostingResponse>> getJobPosting(@PathVariable Long jobId) {
        try {
            JobPostingResponse jobPosting = jobPostingService.getJobPosting(jobId);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tin tuyển dụng thành công", jobPosting));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy danh sách tin tuyển dụng công khai (Public, có phân trang)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobPostingResponse>>> getPublicJobPostings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            PageResponse<JobPostingResponse> jobPostings = jobPostingService.getPublicJobPostings(page, size, sortBy, sortDir);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tin tuyển dụng thành công", jobPostings));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy danh sách tin tuyển dụng của công ty (Employer only)
     */
    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<PageResponse<JobPostingResponse>>> getMyJobPostings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            PageResponse<JobPostingResponse> jobPostings = jobPostingService.getCompanyJobPostings(email, page, size);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tin tuyển dụng của công ty thành công", jobPostings));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Thay đổi trạng thái tin tuyển dụng (Employer only)
     */
    @PatchMapping("/{jobId}/status")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobPostingResponse>> updateJobStatus(
            @PathVariable Long jobId,
            @RequestParam JobStatus status,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            JobPostingResponse jobPosting = jobPostingService.updateJobStatus(email, jobId, status);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái tin tuyển dụng thành công", jobPosting));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Tìm kiếm tin tuyển dụng (Public)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<JobPostingResponse>>> searchJobPostings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false) Integer maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageResponse<JobPostingResponse> jobPostings = jobPostingService.searchJobPostings(
                keyword, location, jobType, minSalary, maxSalary, page, size);
            return ResponseEntity.ok(ApiResponse.success("Tìm kiếm tin tuyển dụng thành công", jobPostings));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Publish tin tuyển dụng (chuyển từ DRAFT sang ACTIVE)
     */
    @PostMapping("/{jobId}/publish")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<JobPostingResponse>> publishJobPosting(
            @PathVariable Long jobId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            JobPostingResponse jobPosting = jobPostingService.updateJobStatus(email, jobId, JobStatus.ACTIVE);
            return ResponseEntity.ok(ApiResponse.success("Đăng tải tin tuyển dụng thành công", jobPosting));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Đóng tin tuyển dụng (chuyển sang CLOSED)
     */
    @PostMapping("/{jobId}/close")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<JobPostingResponse>> closeJobPosting(
            @PathVariable Long jobId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            JobPostingResponse jobPosting = jobPostingService.updateJobStatus(email, jobId, JobStatus.CLOSED);
            return ResponseEntity.ok(ApiResponse.success("Đóng tin tuyển dụng thành công", jobPosting));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}