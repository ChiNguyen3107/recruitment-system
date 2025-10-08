package com.recruitment.system.service;

import com.recruitment.system.dto.response.JobPostingResponse;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.entity.Profile;
import com.recruitment.system.entity.User;
import com.recruitment.system.repository.ApplicationRepository;
import com.recruitment.system.repository.JobPostingRepository;
import com.recruitment.system.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final JobPostingRepository jobPostingRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationRepository applicationRepository;

    @Cacheable(cacheNames = "recommendedJobs", key = "#user.id + '-' + #limit", cacheManager = "longLivedCacheManager")
    public List<JobPostingResponse> getRecommendedJobs(User user, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));

        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        if (profile == null) {
            return Collections.emptyList();
        }

        // Tập job active, chưa hết hạn
        List<JobPosting> activeJobs = jobPostingRepository.findActiveJobs(LocalDateTime.now());

        if (activeJobs.isEmpty()) return Collections.emptyList();

        // Loại trừ đã apply
        Set<Long> appliedJobIds = applicationRepository.findByApplicantId(user.getId())
                .stream().map(a -> a.getJobPosting().getId()).collect(Collectors.toSet());

        // TODO: Saved jobs chưa có model => bỏ qua

        // Chấm điểm
        List<Scored<JobPosting>> scored = new ArrayList<>();
        for (JobPosting job : activeJobs) {
            if (appliedJobIds.contains(job.getId())) continue;
            double score = scoreJob(profile, job);
            if (score > 0) {
                scored.add(new Scored<>(job, score));
            }
        }

        // Sắp xếp DESC theo score
        scored.sort((a, b) -> Double.compare(b.score, a.score));

        // Map sang response kèm matchScore
        return scored.stream().limit(safeLimit)
                .map(s -> toResponse(s.item, s.score))
                .collect(Collectors.toList());
    }

    private JobPostingResponse toResponse(JobPosting job, double score) {
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
        r.setCanApply(job.isActive());
        r.setIsExpired(!job.isActive());
        r.setMatchScore(roundTwo(score));
        return r;
    }

    private double roundTwo(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private double scoreJob(Profile profile, JobPosting job) {
        // Trọng số
        double wSkills = 0.40;
        double wLocation = 0.20;
        double wSalary = 0.20;
        double wJobType = 0.10;
        double wCompanySize = 0.10; // hiện chưa có preference trong Profile, tạm thời 0

        double skillsScore = scoreSkills(profile.getSkills(), job.getSkillsRequired());
        double locationScore = scoreLocation(profile.getDesiredLocation(), job.getLocation());
        double salaryScore = scoreSalary(profile.getDesiredSalaryMin(), profile.getDesiredSalaryMax(), job.getSalaryMin(), job.getSalaryMax());
        double jobTypeScore = scoreJobType(profile.getDesiredJobType(), job.getJobType() == null ? null : job.getJobType().name());
        double companySizeScore = 0.0; // Không có trường preference => 0

        double total = wSkills * skillsScore
                + wLocation * locationScore
                + wSalary * salaryScore
                + wJobType * jobTypeScore
                + wCompanySize * companySizeScore;

        return Math.max(0, Math.min(100.0, total * 100.0));
    }

    private double scoreSkills(String profileSkills, String jobSkills) {
        if (profileSkills == null || profileSkills.isBlank() || jobSkills == null || jobSkills.isBlank()) return 0.0;
        Set<String> userSkills = tokenize(profileSkills);
        Set<String> jobReq = tokenize(jobSkills);
        if (jobReq.isEmpty()) return 0.0;
        long matches = jobReq.stream().filter(userSkills::contains).count();
        double ratio = (double) matches / (double) jobReq.size();
        return clamp01(ratio);
    }

    private double scoreLocation(String desired, String jobLoc) {
        if (jobLoc == null || jobLoc.isBlank()) return 0.0;
        if (desired == null || desired.isBlank()) return 0.5; // không rõ => trung tính
        String d = desired.trim().toLowerCase();
        String j = jobLoc.trim().toLowerCase();
        if (j.equals(d)) return 1.0; // exact
        if (j.contains(d) || d.contains(j)) return 0.6; // partial
        if (j.contains("remote") || d.contains("remote")) return 0.5; // remote fallback
        return 0.0;
    }

    private double scoreSalary(Long desiredMin, Long desiredMax, BigDecimal jobMin, BigDecimal jobMax) {
        if (jobMin == null && jobMax == null) return 0.5; // trung tính
        if (desiredMin == null && desiredMax == null) return 0.5; // trung tính

        long dMin = desiredMin == null ? 0L : desiredMin;
        long dMax = desiredMax == null ? Long.MAX_VALUE : desiredMax;
        long jMin = jobMin == null ? 0L : jobMin.longValue();
        long jMax = jobMax == null ? Long.MAX_VALUE : jobMax.longValue();

        // Tính overlap
        long overlap = Math.max(0L, Math.min(dMax, jMax) - Math.max(dMin, jMin));
        long desiredRange = Math.max(1L, dMax - dMin);
        double ratio = (double) overlap / (double) desiredRange;
        return clamp01(ratio);
    }

    private double scoreJobType(String desiredJobType, String jobJobType) {
        if (jobJobType == null) return 0.0;
        if (desiredJobType == null || desiredJobType.isBlank()) return 0.5;
        return jobJobType.equalsIgnoreCase(desiredJobType) ? 1.0 : 0.0;
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("[^a-z0-9+.#]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    private double clamp01(double v) {
        if (v < 0) return 0.0;
        if (v > 1) return 1.0;
        return v;
    }

    private record Scored<T>(T item, double score) {}
}

