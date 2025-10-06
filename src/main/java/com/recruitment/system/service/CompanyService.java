package com.recruitment.system.service;

import com.recruitment.system.config.AuditLogger;
import com.recruitment.system.dto.request.CompanyUpdateRequest;
import com.recruitment.system.dto.response.CompanyResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.entity.Company;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.repository.ApplicationRepository;
import com.recruitment.system.repository.CompanyRepository;
import com.recruitment.system.repository.JobPostingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ApplicationRepository applicationRepository;
    private final AuditLogger auditLogger;

    @Cacheable(value = "companyPublic", key = "#companyId")
    @Transactional(readOnly = true)
    public CompanyResponse getPublicCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        CompanyResponse resp = mapToResponse(company);

        long activeJobs = jobPostingRepository.countActiveAndNotExpiredByCompany(companyId, LocalDateTime.now());
        resp.setActiveJobsCount((int) activeJobs);

        Long totalApps = applicationRepository.countByCompanyId(companyId);
        Long hiredApps = applicationRepository.countHiredByCompanyId(companyId);
        Double avgResp = applicationRepository.averageResponseDaysByCompany(companyId);
        resp.setAverageResponseTime(avgResp == null ? 0.0 : avgResp);
        double rate = (totalApps == null || totalApps == 0) ? 0.0 : (hiredApps == null ? 0.0 : hiredApps.doubleValue() / totalApps.doubleValue());
        resp.setHiringSuccessRate(rate);

        return resp;
    }

    @Transactional(readOnly = true)
    public List<JobPostingResponse> getCompanyActiveJobs(Long companyId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<JobPosting> page = jobPostingRepository.findActiveJobsByCompany(companyId, LocalDateTime.now(), pageable);
        List<JobPostingResponse> list = new ArrayList<>();
        for (JobPosting jp : page.getContent()) {
            JobPostingResponse jr = new JobPostingResponse();
            jr.setId(jp.getId());
            jr.setTitle(jp.getTitle());
            jr.setDescription(jp.getDescription());
            jr.setRequirements(jp.getRequirements());
            jr.setBenefits(jp.getBenefits());
            jr.setJobType(jp.getJobType());
            jr.setStatus(jp.getStatus());
            jr.setLocation(jp.getLocation());
            jr.setSalaryMin(jp.getSalaryMin());
            jr.setSalaryMax(jp.getSalaryMax());
            jr.setSalaryCurrency(jp.getSalaryCurrency());
            jr.setApplicationDeadline(jp.getApplicationDeadline());
            jr.setPublishedAt(jp.getPublishedAt());
            jr.setViewsCount(jp.getViewsCount());
            jr.setApplicationsCount(jp.getApplicationsCount());
            jr.setCreatedAt(jp.getCreatedAt());
            jr.setUpdatedAt(jp.getUpdatedAt());
            list.add(jr);
        }
        return list;
    }

    @Transactional
    @CacheEvict(value = {"companyPublic", "companyJobs"}, allEntries = true)
    public CompanyResponse updateMyCompany(Long companyId, CompanyUpdateRequest req, String performedByEmail, String ip, String ua) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));

        // Không cho phép thay đổi businessLicense, taxCode (chỉ admin)
        company.setName(req.getName());
        company.setDescription(req.getDescription());
        company.setWorkingHours(req.getWorkingHours());
        company.setBenefits(req.getBenefits());
        company.setCompanyPhotos(req.getCompanyPhotos());
        company.setSocialLinks(req.getSocialLinks());
        company.setCompanySize(req.getCompanySize());
        company.setWebsite(req.getWebsite());
        company.setIndustry(req.getIndustry());
        company.setAddress(req.getAddress());
        company.setCity(req.getCity());
        company.setCountry(req.getCountry());
        company.setPhoneNumber(req.getPhoneNumber());
        company.setContactEmail(req.getContactEmail());

        Company saved = companyRepository.save(company);
        auditLogger.logSecurityEvent("COMPANY_UPDATED", "Company updated: " + saved.getId(), ip, ua);
        return mapToResponse(saved);
    }

    private CompanyResponse mapToResponse(Company c) {
        CompanyResponse r = new CompanyResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setDescription(c.getDescription());
        r.setBusinessLicense(c.getBusinessLicense());
        r.setTaxCode(c.getTaxCode());
        r.setWebsite(c.getWebsite());
        r.setIndustry(c.getIndustry());
        r.setCompanySize(c.getCompanySize());
        r.setAddress(c.getAddress());
        r.setCity(c.getCity());
        r.setCountry(c.getCountry());
        r.setPhoneNumber(c.getPhoneNumber());
        r.setContactEmail(c.getContactEmail());
        r.setLogoUrl(c.getLogoUrl());
        r.setIsVerified(c.getIsVerified());
        r.setCreatedAt(c.getCreatedAt());
        r.setBenefits(c.getBenefits());
        r.setWorkingHours(c.getWorkingHours());
        r.setCompanyPhotos(c.getCompanyPhotos());
        r.setSocialLinks(c.getSocialLinks());
        if (c.getEmployees() != null) r.setEmployeeCount(c.getEmployees().size());
        if (c.getJobPostings() != null) r.setJobPostingCount(c.getJobPostings().size());
        r.setActiveJobsCount(c.getActiveJobsCount());
        r.setAverageResponseTime(c.getAverageResponseTime());
        r.setHiringSuccessRate(c.getHiringSuccessRate());
        return r;
    }
}


