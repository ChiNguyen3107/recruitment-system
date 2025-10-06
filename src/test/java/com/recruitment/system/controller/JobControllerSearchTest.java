package com.recruitment.system.controller;

import com.recruitment.system.entity.Company;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.enums.CompanySize;
import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.enums.JobType;
import com.recruitment.system.repository.JobPostingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JobController.class)
class JobControllerSearchTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobPostingRepository jobPostingRepository;

    private Page<JobPosting> samplePage() {
        JobPosting jp = new JobPosting();
        jp.setId(1L);
        jp.setTitle("Sample");
        jp.setStatus(JobStatus.ACTIVE);
        jp.setApplicationDeadline(LocalDateTime.now().plusDays(10));
        jp.setJobType(JobType.FULL_TIME);
        jp.setSalaryMin(new BigDecimal("10000000"));
        jp.setSalaryMax(new BigDecimal("20000000"));
        Company c = new Company();
        c.setId(1L);
        c.setName("ACME");
        c.setCompanySize(CompanySize.MEDIUM.name());
        jp.setCompany(c);
        return new PageImpl<>(List.of(jp), PageRequest.of(0, 10), 1);
    }

    @Test
    @DisplayName("salaryRange sai định dạng trả 400")
    void salaryRangeInvalidFormat() throws Exception {
        mockMvc.perform(get("/api/jobs/search")
                        .param("salaryRange", "abc-xyz"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("salaryRange không đúng định dạng")));
    }

    @Test
    @DisplayName("postedWithin ngoài khoảng 1-365 trả 400")
    void postedWithinOutOfRange() throws Exception {
        mockMvc.perform(get("/api/jobs/search")
                        .param("postedWithin", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/jobs/search")
                        .param("postedWithin", "366"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("benefits quá 500 ký tự trả 400")
    void benefitsTooLong() throws Exception {
        String longStr = "a".repeat(501);
        mockMvc.perform(get("/api/jobs/search")
                        .param("benefits", longStr))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Tìm kiếm nâng cao với đủ filter trả 200")
    void advancedFiltersOk() throws Exception {
        when(jobPostingRepository.searchAdvancedJobs(
                ArgumentMatchers.eq(JobStatus.ACTIVE),
                ArgumentMatchers.any(LocalDateTime.class),
                ArgumentMatchers.eq("java"),
                ArgumentMatchers.eq("hanoi"),
                ArgumentMatchers.eq(JobType.FULL_TIME),
                ArgumentMatchers.eq(new BigDecimal("10000000")),
                ArgumentMatchers.eq(new BigDecimal("20000000")),
                ArgumentMatchers.any(LocalDateTime.class),
                ArgumentMatchers.eq("senior"),
                ArgumentMatchers.eq("MEDIUM"),
                ArgumentMatchers.eq("remote"),
                ArgumentMatchers.eq("bonus,insurance"),
                ArgumentMatchers.any(Pageable.class)
        )).thenReturn(samplePage());

        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "java")
                        .param("location", "hanoi")
                        .param("jobType", "FULL_TIME")
                        .param("salaryRange", "10000000-20000000")
                        .param("postedWithin", "30")
                        .param("experienceLevel", "SENIOR")
                        .param("companySize", "MEDIUM")
                        .param("workMode", "REMOTE")
                        .param("benefits", "bonus,insurance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Sample"));
    }
}


