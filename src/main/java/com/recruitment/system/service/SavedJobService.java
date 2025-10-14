package com.recruitment.system.service;

import com.recruitment.system.dto.response.ApiResponse;
import com.recruitment.system.dto.response.CompanyResponse;
import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.dto.response.SavedJobActionResponse;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.entity.SavedJob;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.repository.JobPostingRepository;
import com.recruitment.system.repository.SavedJobRepository;
import com.recruitment.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedJobService {
    private final UserRepository userRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobPostingRepository jobPostingRepository;

    private User requireUser(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(()->
                        new NoSuchElementException("User not found"));
    }

    private JobPosting requireActiveJob(Long jobId){
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(()-> new NoSuchElementException("Job not found"));

        boolean active = job.getStatus() == JobStatus.ACTIVE;
        boolean notExpired = job.getApplicationDeadline() == null
                || job.getApplicationDeadline().isAfter(LocalDateTime.now());

        if(!active || !notExpired)
            throw new IllegalStateException("Tin tuyển dụng không hợp lệ hoặc đã hết hạn");
        return job;
    }

    @Transactional
    public ApiResponse<SavedJobActionResponse> saveJob(String email, Long jobId){
        User user = requireUser(email);
        JobPosting job = requireActiveJob(jobId);

        if(savedJobRepository.existsByUserIdAndJobPostingId(user.getId(), jobId)){
            return ApiResponse.error("Bạn đã lưu tin tuyển dụng này rồi");
        }

        SavedJob saved = savedJobRepository.save(
                SavedJob.builder()
                        .userId(user.getId())
                        .jobPostingId(jobId)
                        .savedAt(LocalDateTime.now())
                        .build()
        );

        // TODO: audit log (user saved job)
        SavedJobActionResponse data = SavedJobActionResponse.builder()
                .jobId(jobId)
                .action("SAVED")
                .savedAt(saved.getSavedAt())
                .build();

        return ApiResponse.success("Đã lưu tin tuyển dụng", data);

    }

    @Transactional
    public ApiResponse<SavedJobActionResponse> unsaveJob(String email, Long jobId) {
        User user = requireUser(email);

        SavedJob saved = savedJobRepository.findByUserIdAndJobPostingId(user.getId(), jobId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tin tuyển dụng đã lưu"));

        savedJobRepository.delete(saved);

        // TODO: audit log (user unsaved job)
        SavedJobActionResponse data = SavedJobActionResponse.builder()
                .jobId(jobId)
                .action("UNSAVED")
                .savedAt(null)
                .build();

        return ApiResponse.success("Đã bỏ lưu tin tuyển dụng", data);
    }

    @Transactional(readOnly = true)
    public ApiResponse<Page<JobPostingResponse>> getSavedJobs(String email, int page, int size) {
        User user = requireUser(email);

        Page<SavedJob> savedPage = savedJobRepository
                .findByUserIdOrderBySavedAtDesc(user.getId(), PageRequest.of(page, size));

        // Lấy toàn bộ jobId trong trang này
        List<Long> jobIds = savedPage.getContent().stream()
                .map(SavedJob::getJobPostingId)
                .toList();

        // Fetch job postings theo lô, sau đó lọc ACTIVE + chưa hết hạn
        Map<Long, JobPosting> jobsById = jobPostingRepository.findAllById(jobIds).stream()
                .filter(j -> j.getStatus() == JobStatus.ACTIVE)
                .filter(j -> j.getApplicationDeadline() == null
                        || j.getApplicationDeadline().isAfter(LocalDateTime.now())
                )
                .collect(Collectors.toMap(JobPosting::getId, Function.identity()));

        List<JobPostingResponse> content = new ArrayList<>();
        for (SavedJob s : savedPage) {
            JobPosting job = jobsById.get(s.getJobPostingId());
            if (job == null) continue;

            JobPostingResponse r = toJobPostingResponse(job);

            if (job.getCompany() != null) {
                CompanyResponse c = new CompanyResponse();
                c.setId(job.getCompany().getId());
                c.setName(job.getCompany().getName());
                c.setDescription(job.getCompany().getDescription());
                c.setWebsite(job.getCompany().getWebsite());
                c.setIndustry(job.getCompany().getIndustry());
                c.setAddress(job.getCompany().getAddress());
                r.setCompany(c);
            }

            r.setIsSaved(true);
            r.setSavedAt(s.getSavedAt());
            content.add(r);
        }


        Page<JobPostingResponse> out = new PageImpl<>(
                content,
                savedPage.getPageable(),
                savedPage.getTotalElements() // tổng theo saved (có thể > content nếu filter loại job hết hạn)
        );

        return ApiResponse.success(out);
    }


    private JobPostingResponse toJobPostingResponse(JobPosting job) {
        JobPostingResponse r = new JobPostingResponse();
        r.setId(job.getId());
        r.setTitle(job.getTitle());
        r.setDescription(job.getDescription());
        r.setRequirements(job.getRequirements());
        r.setBenefits(job.getBenefits());
        r.setJobType(job.getJobType());
        r.setStatus(job.getStatus());
        r.setLocation(job.getLocation());
        r.setSalaryMin(job.getSalaryMin());
        r.setSalaryMax(job.getSalaryMax());
        r.setSalaryCurrency(job.getSalaryCurrency());
        r.setExperienceRequired(job.getExperienceRequired());
        r.setEducationRequired(job.getEducationRequired());
        r.setSkillsRequired(job.getSkillsRequired());
        r.setNumberOfPositions(job.getNumberOfPositions());
        r.setApplicationDeadline(job.getApplicationDeadline());
        r.setPublishedAt(job.getPublishedAt());
        r.setViewsCount(job.getViewsCount());
        r.setApplicationsCount(job.getApplicationsCount());
        r.setCreatedAt(job.getCreatedAt());
        r.setUpdatedAt(job.getUpdatedAt());
        return r;
    }

}