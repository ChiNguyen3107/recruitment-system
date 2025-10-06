package com.recruitment.system.controller;

import com.recruitment.system.config.AuditLogger;
import com.recruitment.system.dto.request.CompanyUpdateRequest;
import com.recruitment.system.dto.response.CompanyResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.dto.response.PageResponse;
import com.recruitment.system.entity.User;
import com.recruitment.system.repository.JobPostingRepository;
import com.recruitment.system.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final JobPostingRepository jobPostingRepository;
    private final AuditLogger auditLogger; // reserved for future audit usages

    // 1) Public company profile
    @GetMapping("/{id}/public")
    public ResponseEntity<CompanyPublicResponse> getPublic(@PathVariable("id") Long id) {
        CompanyResponse company = companyService.getPublicCompany(id);
        List<JobPostingResponse> jobs = companyService.getCompanyActiveJobs(id, 10);
        CompanyPublicResponse resp = new CompanyPublicResponse();
        resp.setCompany(company);
        resp.setJobs(jobs);
        return ResponseEntity.ok(resp);
    }

    // 2) Update my company profile - EMPLOYER/RECRUITER of the company
    @PutMapping("/my")
    public ResponseEntity<CompanyResponse> updateMyCompany(@AuthenticationPrincipal UserDetails principal,
                                                           @Valid @RequestBody CompanyUpdateRequest request,
                                                           @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
                                                           @RequestHeader(value = "User-Agent", required = false) String ua) {
        User user = (User) principal; // our User implements UserDetails
        Long companyId = user.getCompany() != null ? user.getCompany().getId() : null;
        if (companyId == null) {
            return ResponseEntity.status(403).build();
        }
        // Chặn thay đổi businessLicense/taxCode bằng validation logic ở service (không set từ request)
        CompanyResponse updated = companyService.updateMyCompany(companyId, request, user.getEmail(), ip, ua);
        return ResponseEntity.ok(updated);
    }

    // 3) Public jobs of a company with pagination
    @GetMapping("/{id}/jobs")
    public ResponseEntity<PageResponse<JobPostingResponse>> getCompanyJobs(@PathVariable("id") Long id,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<JobPostingResponse> mapped = jobPostingRepository
                .findActiveJobsByCompany(id, LocalDateTime.now(), pageable)
                .map(jp -> {
                    JobPostingResponse r = new JobPostingResponse();
                    r.setId(jp.getId());
                    r.setTitle(jp.getTitle());
                    r.setDescription(jp.getDescription());
                    r.setRequirements(jp.getRequirements());
                    r.setBenefits(jp.getBenefits());
                    r.setJobType(jp.getJobType());
                    r.setStatus(jp.getStatus());
                    r.setLocation(jp.getLocation());
                    r.setSalaryMin(jp.getSalaryMin());
                    r.setSalaryMax(jp.getSalaryMax());
                    r.setSalaryCurrency(jp.getSalaryCurrency());
                    r.setApplicationDeadline(jp.getApplicationDeadline());
                    r.setPublishedAt(jp.getPublishedAt());
                    r.setViewsCount(jp.getViewsCount());
                    r.setApplicationsCount(jp.getApplicationsCount());
                    r.setCreatedAt(jp.getCreatedAt());
                    r.setUpdatedAt(jp.getUpdatedAt());
                    return r;
                });

        PageResponse<JobPostingResponse> resp = new PageResponse<>();
        resp.setContent(mapped.getContent());
        resp.setPage(mapped.getNumber());
        resp.setSize(mapped.getSize());
        resp.setTotalElements(mapped.getTotalElements());
        resp.setTotalPages(mapped.getTotalPages());
        return ResponseEntity.ok(resp);
    }

    // Wrapper response for public endpoint
    @lombok.Data
    public static class CompanyPublicResponse {
        private CompanyResponse company;
        private List<JobPostingResponse> jobs;
    }
}


