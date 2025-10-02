package com.recruitment.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.system.config.AuditLogger;
import com.recruitment.system.dto.request.JobPostingRequest;
import com.recruitment.system.dto.request.JobStatusRequest;
import com.recruitment.system.entity.Company;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.enums.JobType;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.enums.UserStatus;
import com.recruitment.system.repository.CompanyRepository;
import com.recruitment.system.repository.JobPostingRepository;
import com.recruitment.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public class ManageJobControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private AuditLogger auditLogger;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Company companyA;
    private Company companyB;
    private User employerA;
    private User employerB;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        companyA = new Company();
        companyA.setName("Company A");
        companyRepository.save(companyA);

        companyB = new Company();
        companyB.setName("Company B");
        companyRepository.save(companyB);

        employerA = new User();
        employerA.setEmail("empA@example.com");
        employerA.setFirstName("Emp");
        employerA.setLastName("A");
        employerA.setRole(UserRole.EMPLOYER);
        employerA.setStatus(UserStatus.ACTIVE);
        employerA.setEmailVerified(true);
        employerA.setCompany(companyA);
        userRepository.save(employerA);

        employerB = new User();
        employerB.setEmail("empB@example.com");
        employerB.setFirstName("Emp");
        employerB.setLastName("B");
        employerB.setRole(UserRole.EMPLOYER);
        employerB.setStatus(UserStatus.ACTIVE);
        employerB.setEmailVerified(true);
        employerB.setCompany(companyB);
        userRepository.save(employerB);
    }

    private JobPostingRequest buildValidRequest(JobStatus status) {
        JobPostingRequest req = new JobPostingRequest();
        req.setTitle("Java Developer");
        req.setDescription("Mô tả công việc chi tiết");
        req.setRequirements("Yêu cầu");
        req.setBenefits("Quyền lợi");
        req.setJobType(JobType.FULL_TIME);
        req.setStatus(status);
        req.setLocation("Hà Nội");
        req.setSalaryMin(new BigDecimal("1000"));
        req.setSalaryMax(new BigDecimal("2000"));
        req.setSalaryCurrency("VND");
        req.setExperienceRequired("2+ năm");
        req.setEducationRequired("Đại học");
        req.setSkillsRequired("Java, Spring");
        req.setNumberOfPositions(2);
        req.setApplicationDeadline(LocalDateTime.now().plusDays(10));
        return req;
    }

    @Test
    void createJob_happy_active_withFutureDeadline() throws Exception {
        JobPostingRequest req = buildValidRequest(JobStatus.ACTIVE);

        mockMvc.perform(post("/api/jobs/manage")
                        .with(user(employerA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.company.id").exists())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void createJob_invalid_deadlinePast_should400() throws Exception {
        JobPostingRequest req = buildValidRequest(JobStatus.ACTIVE);
        req.setApplicationDeadline(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/jobs/manage")
                        .with(user(employerA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateJob_forbidden_otherCompany_shouldFail() throws Exception {
        // Tạo job thuộc company A
        JobPosting job = new JobPosting();
        job.setTitle("Tin A");
        job.setDescription("Mô tả");
        job.setJobType(JobType.FULL_TIME);
        job.setStatus(JobStatus.DRAFT);
        job.setApplicationDeadline(LocalDateTime.now().plusDays(5));
        job.setCompany(companyA);
        job.setCreatedBy(employerA);
        jobPostingRepository.save(job);

        JobPostingRequest updateReq = buildValidRequest(JobStatus.DRAFT);
        updateReq.setTitle("Tin đã sửa");

        mockMvc.perform(put("/api/jobs/manage/" + job.getId())
                        .with(user(employerB)) // khác công ty
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isBadRequest()) // controller hiện trả 400 cho ownership
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteJob_soft_happy() throws Exception {
        JobPosting job = new JobPosting();
        job.setTitle("Tin A");
        job.setDescription("Mô tả");
        job.setJobType(JobType.FULL_TIME);
        job.setStatus(JobStatus.ACTIVE);
        job.setApplicationDeadline(LocalDateTime.now().plusDays(5));
        job.setCompany(companyA);
        job.setCreatedBy(employerA);
        jobPostingRepository.save(job);

        mockMvc.perform(delete("/api/jobs/manage/" + job.getId())
                        .with(user(employerA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        JobPosting updated = jobPostingRepository.findById(job.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(JobStatus.CLOSED);
    }

    @Test
    void deleteJob_hard_happy() throws Exception {
        JobPosting job = new JobPosting();
        job.setTitle("Tin A");
        job.setDescription("Mô tả");
        job.setJobType(JobType.FULL_TIME);
        job.setStatus(JobStatus.DRAFT);
        job.setApplicationDeadline(LocalDateTime.now().plusDays(5));
        job.setCompany(companyA);
        job.setCreatedBy(employerA);
        jobPostingRepository.save(job);

        mockMvc.perform(delete("/api/jobs/manage/" + job.getId())
                        .param("hard", "true")
                        .with(user(employerA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(jobPostingRepository.findById(job.getId())).isEmpty();
    }

    @Test
    void patchStatus_active_withPastDeadline_should400() throws Exception {
        JobPosting job = new JobPosting();
        job.setTitle("Tin A");
        job.setDescription("Mô tả");
        job.setJobType(JobType.FULL_TIME);
        job.setStatus(JobStatus.DRAFT);
        job.setApplicationDeadline(LocalDateTime.now().minusDays(1));
        job.setCompany(companyA);
        job.setCreatedBy(employerA);
        jobPostingRepository.save(job);

        JobStatusRequest req = new JobStatusRequest();
        req.setStatus(JobStatus.ACTIVE);

        mockMvc.perform(patch("/api/jobs/manage/" + job.getId() + "/status")
                        .with(user(employerA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}


