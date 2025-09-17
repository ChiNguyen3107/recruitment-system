package com.recruitment.system.service;

import com.recruitment.system.dto.request.JobPostingRequest;
import com.recruitment.system.dto.response.CompanyResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.dto.response.PageResponse;
import com.recruitment.system.dto.response.UserResponse;
import com.recruitment.system.entity.Company;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.repository.JobPostingRepository;
import com.recruitment.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý quản lý tin tuyển dụng
 */
@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;

    /**
     * Tạo tin tuyển dụng mới (Employer only)
     */
    @Transactional
    public JobPostingResponse createJobPosting(String email, JobPostingRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Kiểm tra quyền Employer
        if (user.getRole() != UserRole.EMPLOYER && user.getRole() != UserRole.RECRUITER) {
            throw new RuntimeException("Chỉ Employer/Recruiter mới có thể tạo tin tuyển dụng");
        }
        
        Company company = user.getCompany();
        if (company == null) {
            throw new RuntimeException("User chưa có thông tin công ty");
        }

        JobPosting jobPosting = new JobPosting();
        updateJobPostingFromRequest(jobPosting, request);
        jobPosting.setCompany(company);
        jobPosting.setCreatedBy(user);
        jobPosting.setStatus(JobStatus.ACTIVE);
        jobPosting.setCreatedAt(LocalDateTime.now());
        jobPosting.setUpdatedAt(LocalDateTime.now());
        
        jobPosting = jobPostingRepository.save(jobPosting);
        return convertToJobPostingResponse(jobPosting);
    }

    /**
     * Cập nhật tin tuyển dụng (Employer only)
     */
    @Transactional
    public JobPostingResponse updateJobPosting(String email, Long jobId, JobPostingRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job posting not found"));
        
        // Kiểm tra quyền: chỉ người tạo hoặc admin mới được sửa
        if (!jobPosting.getCreatedBy().equals(user) && user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa tin tuyển dụng này");
        }
        
        updateJobPostingFromRequest(jobPosting, request);
        jobPosting.setUpdatedAt(LocalDateTime.now());
        
        jobPosting = jobPostingRepository.save(jobPosting);
        return convertToJobPostingResponse(jobPosting);
    }

    /**
     * Xóa tin tuyển dụng (Employer only)
     */
    @Transactional
    public void deleteJobPosting(String email, Long jobId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job posting not found"));
        
        // Kiểm tra quyền: chỉ người tạo hoặc admin mới được xóa
        if (!jobPosting.getCreatedBy().equals(user) && user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Bạn không có quyền xóa tin tuyển dụng này");
        }
        
        jobPostingRepository.delete(jobPosting);
    }

    /**
     * Lấy chi tiết tin tuyển dụng
     */
    public JobPostingResponse getJobPosting(Long jobId) {
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job posting not found"));
        
        return convertToJobPostingResponse(jobPosting);
    }

    /**
     * Lấy danh sách tin tuyển dụng công khai (có phân trang)
     */
    public PageResponse<JobPostingResponse> getPublicJobPostings(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<JobPosting> jobPage = jobPostingRepository.findByStatus(JobStatus.ACTIVE, pageable);
        
        List<JobPostingResponse> jobResponses = jobPage.getContent().stream()
                .map(this::convertToJobPostingResponse)
                .collect(Collectors.toList());
        
        return createPageResponse(jobPage, jobResponses);
    }

    /**
     * Lấy danh sách tin tuyển dụng của công ty (Employer only)
     */
    public PageResponse<JobPostingResponse> getCompanyJobPostings(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Company company = user.getCompany();
        if (company == null) {
            throw new RuntimeException("User chưa có thông tin công ty");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobPosting> jobPage = jobPostingRepository.findByCompany(company, pageable);
        
        List<JobPostingResponse> jobResponses = jobPage.getContent().stream()
                .map(this::convertToJobPostingResponse)
                .collect(Collectors.toList());
        
        return createPageResponse(jobPage, jobResponses);
    }

    /**
     * Thay đổi trạng thái tin tuyển dụng
     */
    @Transactional
    public JobPostingResponse updateJobStatus(String email, Long jobId, JobStatus status) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job posting not found"));
        
        // Kiểm tra quyền
        if (!jobPosting.getCreatedBy().equals(user) && user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Bạn không có quyền thay đổi trạng thái tin tuyển dụng này");
        }
        
        jobPosting.setStatus(status);
        jobPosting.setUpdatedAt(LocalDateTime.now());
        
        jobPosting = jobPostingRepository.save(jobPosting);
        return convertToJobPostingResponse(jobPosting);
    }

    /**
     * Tìm kiếm tin tuyển dụng theo từ khóa
     */
    public PageResponse<JobPostingResponse> searchJobPostings(String keyword, String location, 
                                                             String jobType, Integer minSalary, 
                                                             Integer maxSalary, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<JobPosting> jobPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            jobPage = jobPostingRepository.findByTitleContainingIgnoreCaseAndStatus(keyword, JobStatus.ACTIVE, pageable);
        } else {
            jobPage = jobPostingRepository.findByStatus(JobStatus.ACTIVE, pageable);
        }
        
        List<JobPostingResponse> jobResponses = jobPage.getContent().stream()
                .map(this::convertToJobPostingResponse)
                .collect(Collectors.toList());
        
        return createPageResponse(jobPage, jobResponses);
    }

    /**
     * Cập nhật JobPosting từ request
     */
    private void updateJobPostingFromRequest(JobPosting jobPosting, JobPostingRequest request) {
        if (request.getTitle() != null) {
            jobPosting.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            jobPosting.setDescription(request.getDescription());
        }
        if (request.getRequirements() != null) {
            jobPosting.setRequirements(request.getRequirements());
        }
        if (request.getLocation() != null) {
            jobPosting.setLocation(request.getLocation());
        }
        if (request.getSalaryMin() != null) {
            jobPosting.setSalaryMin(request.getSalaryMin());
        }
        if (request.getSalaryMax() != null) {
            jobPosting.setSalaryMax(request.getSalaryMax());
        }
        if (request.getJobType() != null) {
            jobPosting.setJobType(request.getJobType());
        }
        if (request.getExperienceRequired() != null) {
            jobPosting.setExperienceRequired(request.getExperienceRequired());
        }
        if (request.getEducationRequired() != null) {
            jobPosting.setEducationRequired(request.getEducationRequired());
        }
        if (request.getSkillsRequired() != null) {
            jobPosting.setSkillsRequired(request.getSkillsRequired());
        }
        if (request.getBenefits() != null) {
            jobPosting.setBenefits(request.getBenefits());
        }
        if (request.getApplicationDeadline() != null) {
            jobPosting.setApplicationDeadline(request.getApplicationDeadline());
        }
        if (request.getSalaryCurrency() != null) {
            jobPosting.setSalaryCurrency(request.getSalaryCurrency());
        }
        if (request.getNumberOfPositions() != null) {
            jobPosting.setNumberOfPositions(request.getNumberOfPositions());
        }
    }

    /**
     * Convert JobPosting entity to JobPostingResponse
     */
    private JobPostingResponse convertToJobPostingResponse(JobPosting jobPosting) {
        JobPostingResponse response = new JobPostingResponse();
        response.setId(jobPosting.getId());
        response.setTitle(jobPosting.getTitle());
        response.setDescription(jobPosting.getDescription());
        response.setRequirements(jobPosting.getRequirements());
        response.setLocation(jobPosting.getLocation());
        response.setSalaryMin(jobPosting.getSalaryMin());
        response.setSalaryMax(jobPosting.getSalaryMax());
        response.setSalaryCurrency(jobPosting.getSalaryCurrency());
        response.setJobType(jobPosting.getJobType());
        response.setExperienceRequired(jobPosting.getExperienceRequired());
        response.setEducationRequired(jobPosting.getEducationRequired());
        response.setSkillsRequired(jobPosting.getSkillsRequired());
        response.setBenefits(jobPosting.getBenefits());
        response.setStatus(jobPosting.getStatus());
        response.setApplicationDeadline(jobPosting.getApplicationDeadline());
        response.setPublishedAt(jobPosting.getPublishedAt());
        response.setNumberOfPositions(jobPosting.getNumberOfPositions());
        response.setViewsCount(jobPosting.getViewsCount());
        response.setApplicationsCount(jobPosting.getApplicationsCount());
        response.setCreatedAt(jobPosting.getCreatedAt());
        response.setUpdatedAt(jobPosting.getUpdatedAt());
        
        // Tính toán trạng thái
        response.setCanApply(jobPosting.canReceiveApplications());
        response.setIsExpired(jobPosting.getApplicationDeadline() != null && 
                            jobPosting.getApplicationDeadline().isBefore(LocalDateTime.now()));
        
        // Thông tin công ty
        Company company = jobPosting.getCompany();
        if (company != null) {
            CompanyResponse companyResponse = new CompanyResponse();
            companyResponse.setId(company.getId());
            companyResponse.setName(company.getName());
            companyResponse.setDescription(company.getDescription());
            companyResponse.setWebsite(company.getWebsite());
            companyResponse.setIndustry(company.getIndustry());
            companyResponse.setAddress(company.getAddress());
            response.setCompany(companyResponse);
        }
        
        // Thông tin người tạo
        User createdBy = jobPosting.getCreatedBy();
        if (createdBy != null) {
            UserResponse userResponse = new UserResponse();
            userResponse.setId(createdBy.getId());
            userResponse.setEmail(createdBy.getEmail());
            userResponse.setFirstName(createdBy.getFirstName());
            userResponse.setLastName(createdBy.getLastName());
            userResponse.setFullName(createdBy.getFirstName() + " " + createdBy.getLastName());
            userResponse.setRole(createdBy.getRole());
            response.setCreatedBy(userResponse);
        }
        
        return response;
    }
    
    /**
     * Helper method để tạo PageResponse
     */
    private PageResponse<JobPostingResponse> createPageResponse(Page<JobPosting> jobPage, List<JobPostingResponse> jobResponses) {
        PageResponse<JobPostingResponse> response = new PageResponse<>();
        response.setContent(jobResponses);
        response.setPage(jobPage.getNumber());
        response.setSize(jobPage.getSize());
        response.setTotalElements(jobPage.getTotalElements());
        response.setTotalPages(jobPage.getTotalPages());
        response.setFirst(jobPage.isFirst());
        response.setLast(jobPage.isLast());
        response.setHasNext(jobPage.hasNext());
        response.setHasPrevious(jobPage.hasPrevious());
        return response;
    }
}