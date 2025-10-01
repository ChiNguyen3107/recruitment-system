package com.recruitment.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.system.dto.request.ApplicationRequest;
import com.recruitment.system.entity.JobPosting;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.JobStatus;
import com.recruitment.system.enums.JobType;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.enums.UserStatus;
import com.recruitment.system.repository.ApplicationRepository;
import com.recruitment.system.repository.JobPostingRepository;
import com.recruitment.system.service.MailService;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test validation và sanitization cho ApplicationController
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public class ApplicationControllerValidationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @MockBean
    private MailService mailService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;
    private JobPosting testJobPosting;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        // Tạo test user
        testUser = new User();
        testUser.setId(888L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.APPLICANT);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setEmailVerified(true);

        // Tạo test job posting
        testJobPosting = new JobPosting();
        testJobPosting.setId(888L);
        testJobPosting.setTitle("Test Job");
        testJobPosting.setDescription("Test Description");
        testJobPosting.setStatus(JobStatus.ACTIVE);
        testJobPosting.setJobType(JobType.FULL_TIME);
        testJobPosting.setApplicationDeadline(LocalDateTime.now().plusDays(30));
        testJobPosting.setPublishedAt(LocalDateTime.now());
        testJobPosting.setCreatedBy(testUser);
        testJobPosting.setApplicationsCount(0);

        jobPostingRepository.save(testJobPosting);
    }

    @Test
    void testValidApplicationRequest() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobPostingId(testJobPosting.getId());
        request.setCoverLetter("Tôi rất quan tâm đến vị trí này");
        request.setResumeUrl("https://example.com/cv.pdf");
        request.setAdditionalDocuments("Bằng cấp bổ sung");

        mockMvc.perform(post("/api/applications/my")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Nộp đơn thành công"));
    }

    @Test
    void testInvalidJobPostingId() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobPostingId(99999L); // Job không tồn tại
        request.setCoverLetter("Tôi rất quan tâm đến vị trí này");

        mockMvc.perform(post("/api/applications/my")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // RuntimeException được throw
    }

    @Test
    void testNullJobPostingId() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobPostingId(null);
        request.setCoverLetter("Tôi rất quan tâm đến vị trí này");

        mockMvc.perform(post("/api/applications/my")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("ID tin tuyển dụng không được để trống")));
    }

    @Test
    void testCoverLetterTooLong() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobPostingId(testJobPosting.getId());
        request.setCoverLetter("A".repeat(5001)); // Vượt quá 5000 ký tự

        mockMvc.perform(post("/api/applications/my")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Thư xin việc không được quá 5000 ký tự")));
    }

    @Test
    void testInvalidResumeUrl() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobPostingId(testJobPosting.getId());
        request.setCoverLetter("Tôi rất quan tâm đến vị trí này");
        request.setResumeUrl("invalid-url"); // URL không hợp lệ

        mockMvc.perform(post("/api/applications/my")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("URL CV không hợp lệ")));
    }

    @Test
    void testResumeUrlTooLong() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobPostingId(testJobPosting.getId());
        request.setCoverLetter("Tôi rất quan tâm đến vị trí này");
        request.setResumeUrl("https://example.com/" + "a".repeat(500)); // URL quá dài

        mockMvc.perform(post("/api/applications/my")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("URL CV không được quá 500 ký tự")));
    }

    @Test
    void testAdditionalDocumentsTooLong() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobPostingId(testJobPosting.getId());
        request.setCoverLetter("Tôi rất quan tâm đến vị trí này");
        request.setAdditionalDocuments("A".repeat(1001)); // Vượt quá 1000 ký tự

        mockMvc.perform(post("/api/applications/my")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Tài liệu bổ sung không được quá 1000 ký tự")));
    }

    @Test
    void testXssSanitization() throws Exception {
        ApplicationRequest request = new ApplicationRequest();
        request.setJobPostingId(testJobPosting.getId());
        request.setCoverLetter("<script>alert('xss')</script>Tôi rất quan tâm đến vị trí này");
        request.setAdditionalDocuments("<img src=x onerror=alert('xss')>Tài liệu bổ sung");

        mockMvc.perform(post("/api/applications/my")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Kiểm tra trong database đã được sanitize
        var applications = applicationRepository.findAll();
        assertEquals(1, applications.size());
        
        var application = applications.get(0);
        assertTrue(application.getCoverLetter().contains("Tôi rất quan tâm đến vị trí này"));
        assertFalse(application.getCoverLetter().contains("<script>"));
        assertTrue(application.getAdditionalDocuments().contains("Tài liệu bổ sung"));
        assertFalse(application.getAdditionalDocuments().contains("<img"));
    }

    @Test
    void testDuplicateApplication() throws Exception {
        // Nộp đơn lần đầu
        ApplicationRequest request = new ApplicationRequest();
        request.setJobPostingId(testJobPosting.getId());
        request.setCoverLetter("Tôi rất quan tâm đến vị trí này");

        mockMvc.perform(post("/api/applications/my")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Nộp đơn lần thứ hai - nên trả lỗi 409
        mockMvc.perform(post("/api/applications/my")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bạn đã nộp đơn cho vị trí này trước đó"));
    }
}
