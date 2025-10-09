package com.recruitment.system.service;

import com.recruitment.system.dto.response.JobStatsResponse;
import com.recruitment.system.dto.response.MonthlyStats;
import com.recruitment.system.repository.ApplicationRepository;
import com.recruitment.system.repository.JobPostingRepository;
import com.recruitment.system.repository.CompanyRepository;
import com.recruitment.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    // ================= Employer Metrics =================

    @Cacheable(value = "employerDashboard", key = "#companyId + '-' + (#from == null ? 'null' : #from.toString()) + '-' + (#to == null ? 'null' : #to.toString()) + '-' + (#limit == null ? 5 : #limit)")
    @Transactional(readOnly = true)
    public Map<String, Object> buildEmployerMetrics(Long companyId, java.time.LocalDateTime from, java.time.LocalDateTime to, Integer limit) {
        Map<String, Object> payload = new HashMap<>();
        int topLimit = (limit == null || limit <= 0) ? 5 : limit;

        // 1) applicationsByStatus
        Map<String, Integer> appsByStatus = new LinkedHashMap<>();
        List<Object[]> rows = (from == null && to == null)
                ? applicationRepository.countApplicationsByStatusForCompany(companyId)
                : applicationRepository.countApplicationsByStatusForCompanyInRange(companyId, from, to);
        for (Object[] r : rows) {
            String status = String.valueOf(r[0]);
            Number c = (Number) r[1];
            appsByStatus.put(status, c == null ? 0 : c.intValue());
        }
        payload.put("applicationsByStatus", appsByStatus);

        // 2) topPerformingJobs (top 5)
        List<Object[]> topRows = (from == null && to == null)
                ? applicationRepository.findTopJobsByApplicationsForCompany(companyId, topLimit)
                : applicationRepository.findTopJobsByApplicationsForCompanyInRange(companyId, topLimit, from, to);
        List<JobStatsResponse> topJobs = new ArrayList<>();
        for (Object[] r : topRows) {
            String jobTitle = (String) r[0];
            long apps = r[1] == null ? 0L : ((Number) r[1]).longValue();
            long interviews = r[2] == null ? 0L : ((Number) r[2]).longValue();
            long hires = r[3] == null ? 0L : ((Number) r[3]).longValue();
            double rate = apps == 0 ? 0.0 : (double) hires / (double) apps;
            JobStatsResponse jobStats = new JobStatsResponse();
            jobStats.setJobId(null);
            jobStats.setJobTitle(jobTitle);
            jobStats.setApplicationsCount(apps);
            jobStats.setInterviewsCount(interviews);
            jobStats.setHiresCount(hires);
            jobStats.setConversionRate(rate);
            topJobs.add(jobStats);
        }
        payload.put("topPerformingJobs", topJobs);

        // 3) averageTimeToHire
        Double avg = (from == null && to == null)
                ? applicationRepository.averageDaysFromReceivedToHired(companyId)
                : applicationRepository.averageDaysFromReceivedToHiredInRange(companyId, from, to);
        payload.put("averageTimeToHire", avg == null ? 0.0 : avg);

        // 4) conversionRates
        Object[] totals = (from == null && to == null)
                ? applicationRepository.countStageTotalsForCompany(companyId)
                : applicationRepository.countStageTotalsForCompanyInRange(companyId, from, to);
        long recv = getLong(totals, 0);
        long rev = getLong(totals, 1);
        long intr = getLong(totals, 2);
        long off = getLong(totals, 3);
        long hire = getLong(totals, 4);
        Map<String, Double> conv = new LinkedHashMap<>();
        conv.put("receivedToReviewed", ratio(rev, recv));
        conv.put("reviewedToInterview", ratio(intr, rev));
        conv.put("interviewToOffer", ratio(off, intr));
        conv.put("offerToHired", ratio(hire, off));
        payload.put("conversionRates", conv);

        // 5) hiringTrend 12 tháng gần nhất
        List<Object[]> trendRows = (from == null && to == null)
                ? applicationRepository.hiringTrendLast12MonthsForCompany(companyId)
                : applicationRepository.hiringTrendForCompanyInRange(companyId, from, to);
        List<MonthlyStats> trend = new ArrayList<>();
        for (Object[] r : trendRows) {
            int y = ((Number) r[0]).intValue();
            int m = ((Number) r[1]).intValue();
            long c = ((Number) r[2]).longValue();
            trend.add(new MonthlyStats(y, m, c));
        }
        payload.put("hiringTrend", trend);

        return payload;
    }

    private static long getLong(Object[] arr, int idx) {
        if (arr == null || arr.length <= idx || arr[idx] == null) return 0L;
        Object val = arr[idx];
        if (val instanceof Number) {
            return ((Number) val).longValue();
        } else if (val instanceof Object[]) {
            // Handle nested array case
            Object[] nested = (Object[]) val;
            if (nested.length > 0 && nested[0] instanceof Number) {
                return ((Number) nested[0]).longValue();
            }
        }
        return 0L;
    }

    private static double ratio(long numerator, long denominator) {
        return denominator == 0 ? 0.0 : (double) numerator / (double) denominator;
    }

    // ================= Admin Metrics =================

    @Cacheable(value = "adminDashboard", key = "'overview-' + (#from == null ? 'null' : #from.toString()) + '-' + (#to == null ? 'null' : #to.toString())")
    @Transactional(readOnly = true)
    public Map<String, Object> buildAdminOverview(java.time.LocalDateTime from, java.time.LocalDateTime to) {
        Map<String, Object> payload = new HashMap<>();

        // Users
        long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countActiveUsersLast30Days();
        Long newUsersThisMonth = userRepository.countNewUsersThisMonth();
        payload.put("totalUsers", totalUsers);
        payload.put("activeUsers", activeUsers == null ? 0L : activeUsers);
        payload.put("newUsersThisMonth", newUsersThisMonth == null ? 0L : newUsersThisMonth);

        // Companies
        long totalCompanies = companyRepository.count();
        Long verifiedCompanies = companyRepository.countVerifiedCompanies();
        Long companiesWithActiveJobs = jobPostingRepository.countDistinctCompaniesWithActiveJobs();
        payload.put("totalCompanies", totalCompanies);
        payload.put("verifiedCompanies", verifiedCompanies == null ? 0L : verifiedCompanies);
        payload.put("companiesWithActiveJobs", companiesWithActiveJobs == null ? 0L : companiesWithActiveJobs);

        // Jobs overview
        Map<String, Long> jobsByStatus = new LinkedHashMap<>();
        for (Object[] r : jobPostingRepository.countJobsByStatusGroup()) {
            jobsByStatus.put(String.valueOf(r[0]), ((Number) r[1]).longValue());
        }
        payload.put("totalJobsByStatus", jobsByStatus);
        payload.put("jobsPostedThisMonth", jobPostingRepository.countJobsPostedThisMonth());

        // Applications overview
        Map<String, Long> appsByStatus = new LinkedHashMap<>();
        for (Object[] r : (from == null && to == null)
                ? applicationRepository.countApplicationsByStatusGroup()
                : applicationRepository.countApplicationsByStatusInRange(from, to)) {
            appsByStatus.put(String.valueOf(r[0]), ((Number) r[1]).longValue());
        }
        payload.put("totalApplicationsByStatus", appsByStatus);
        payload.put("applicationsThisMonth", applicationRepository.countApplicationsThisMonth());

        return payload;
    }

    @Cacheable(value = "adminDashboard", key = "'performance-' + (#from == null ? 'null' : #from.toString()) + '-' + (#to == null ? 'null' : #to.toString())")
    @Transactional(readOnly = true)
    public Map<String, Object> buildAdminPerformance(java.time.LocalDateTime from, java.time.LocalDateTime to) {
        Map<String, Object> payload = new HashMap<>();

        // averageApplicationsPerJob
        long totalApps = applicationRepository.count();
        long totalJobs = jobPostingRepository.count();
        payload.put("averageApplicationsPerJob", totalJobs == 0 ? 0.0 : (double) totalApps / (double) totalJobs);

        // averageJobsPerCompany
        long totalCompanies = companyRepository.count();
        payload.put("averageJobsPerCompany", totalCompanies == 0 ? 0.0 : (double) totalJobs / (double) totalCompanies);

        // systemWideConversionRate: HIRED / total applications
        Map<String, Long> appsByStatus = new LinkedHashMap<>();
        for (Object[] r : (from == null && to == null)
                ? applicationRepository.countApplicationsByStatusGroup()
                : applicationRepository.countApplicationsByStatusInRange(from, to)) {
            appsByStatus.put(String.valueOf(r[0]), ((Number) r[1]).longValue());
        }
        long hired = appsByStatus.getOrDefault("HIRED", 0L);
        payload.put("systemWideConversionRate", totalApps == 0 ? 0.0 : (double) hired / (double) totalApps);

        // mostPopularJobTypes (top 5)
        List<Map<String, Object>> popularTypes = new ArrayList<>();
        for (Object[] r : jobPostingRepository.mostPopularJobTypesTop5()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("jobType", r[0] == null ? null : String.valueOf(r[0]));
            item.put("count", ((Number) r[1]).longValue());
            popularTypes.add(item);
        }
        payload.put("mostPopularJobTypes", popularTypes);

        // mostActiveCompanies (top 10)
        List<Map<String, Object>> mostActiveCompanies = new ArrayList<>();
        for (Object[] r : applicationRepository.mostActiveCompanies(10)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("companyId", ((Number) r[0]).longValue());
            item.put("companyName", r[1]);
            item.put("applications", ((Number) r[2]).longValue());
            mostActiveCompanies.add(item);
        }
        payload.put("mostActiveCompanies", mostActiveCompanies);

        return payload;
    }

    @Cacheable(value = "adminDashboard", key = "'growth-' + (#from == null ? 'null' : #from.toString()) + '-' + (#to == null ? 'null' : #to.toString())")
    @Transactional(readOnly = true)
    public Map<String, Object> buildAdminGrowth(java.time.LocalDateTime from, java.time.LocalDateTime to) {
        Map<String, Object> payload = new HashMap<>();

        // applicationVolumeTrend
        List<Object[]> appRows = (from == null && to == null)
                ? applicationRepository.applicationVolumeTrendLast12Months()
                : applicationRepository.applicationVolumeTrendInRange(from, to);
        payload.put("applicationVolumeTrend", mapMonthly(appRows));

        // jobPostingTrend
        List<Object[]> jobRows = (from == null && to == null)
                ? jobPostingRepository.jobPostingTrendLast12Months()
                : jobPostingRepository.jobPostingTrendInRange(from, to);
        payload.put("jobPostingTrend", mapMonthly(jobRows));

        // userGrowthChart
        List<Object[]> userRows = (from == null && to == null)
                ? userRepository.userGrowthLast12Months()
                : userRepository.userGrowthInRange(from, to);
        payload.put("userGrowthChart", mapMonthly(userRows));

        return payload;
    }

    private static List<MonthlyStats> mapMonthly(List<Object[]> rows) {
        List<MonthlyStats> list = new ArrayList<>();
        for (Object[] r : rows) {
            int y = ((Number) r[0]).intValue();
            int m = ((Number) r[1]).intValue();
            long c = ((Number) r[2]).longValue();
            list.add(new MonthlyStats(y, m, c));
        }
        return list;
    }
}


