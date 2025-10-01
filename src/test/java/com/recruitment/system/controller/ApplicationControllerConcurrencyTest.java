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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Test concurrency cho ApplicationController để đảm bảo chống nộp đơn trùng
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public class ApplicationControllerConcurrencyTest {

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
        testUser.setId(999L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.APPLICANT);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setEmailVerified(true);

        // Tạo test job posting
        testJobPosting = new JobPosting();
        testJobPosting.setId(999L);
        testJobPosting.setTitle("Test Job");
        testJobPosting.setDescription("Test Description");
        testJobPosting.setStatus(JobStatus.ACTIVE);
        testJobPosting.setJobType(JobType.FULL_TIME);
        testJobPosting.setApplicationDeadline(LocalDateTime.now().plusDays(30));
        testJobPosting.setPublishedAt(LocalDateTime.now());
        testJobPosting.setCreatedBy(testUser);
        testJobPosting.setApplicationsCount(0);

        // Lưu test data
        jobPostingRepository.save(testJobPosting);
    }

    @Test
    void testConcurrentApplicationSubmission_OnlyOneShouldSucceed() throws Exception {
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        ApplicationRequest request = new ApplicationRequest();
        request.setJobPostingId(testJobPosting.getId());
        request.setCoverLetter("Test cover letter");
        request.setResumeUrl("http://example.com/resume.pdf");

        // Tạo các task song song
        CompletableFuture<?>[] futures = new CompletableFuture[numberOfThreads];
        
        for (int i = 0; i < numberOfThreads; i++) {
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    latch.countDown();
                    latch.await(); // Đợi tất cả thread sẵn sàng

                    mockMvc.perform(post("/api/applications/my")
                            .with(user(testUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(result -> {
                                int status = result.getResponse().getStatus();
                                if (status == 200) {
                                    successCount.incrementAndGet();
                                } else if (status == 409) {
                                    conflictCount.incrementAndGet();
                                }
                            });
                } catch (Exception e) {
                    // Log lỗi nhưng không fail test
                    System.err.println("Thread error: " + e.getMessage());
                }
            }, executor);
        }

        // Đợi tất cả task hoàn thành
        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        // Kiểm tra kết quả
        assertEquals(1, successCount.get(), "Chỉ nên có 1 đơn ứng tuyển thành công");
        assertEquals(numberOfThreads - 1, conflictCount.get(), "Các request còn lại nên trả lỗi 409");
        
        // Kiểm tra trong database chỉ có 1 application
        long applicationCount = applicationRepository.count();
        assertEquals(1, applicationCount, "Database chỉ nên có 1 application");
    }

    @Test
    void testConcurrentApplicationSubmission_DifferentUsers_AllShouldSucceed() throws Exception {
        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        // Tạo các user khác nhau
        User[] users = new User[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            users[i] = new User();
            users[i].setId(1000L + i);
            users[i].setEmail("test" + i + "@example.com");
            users[i].setFirstName("Test" + i);
            users[i].setLastName("User");
            users[i].setRole(UserRole.APPLICANT);
            users[i].setStatus(UserStatus.ACTIVE);
            users[i].setEmailVerified(true);
        }

        // Tạo các task song song với user khác nhau
        CompletableFuture<?>[] futures = new CompletableFuture[numberOfThreads];
        
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    latch.countDown();
                    latch.await(); // Đợi tất cả thread sẵn sàng

                    ApplicationRequest request = new ApplicationRequest();
                    request.setJobPostingId(testJobPosting.getId());
                    request.setCoverLetter("Test cover letter from user " + index);
                    request.setResumeUrl("http://example.com/resume" + index + ".pdf");

                    mockMvc.perform(post("/api/applications/my")
                            .with(user(users[index]))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(result -> {
                                int status = result.getResponse().getStatus();
                                if (status == 200) {
                                    successCount.incrementAndGet();
                                }
                            });
                } catch (Exception e) {
                    System.err.println("Thread error: " + e.getMessage());
                }
            }, executor);
        }

        // Đợi tất cả task hoàn thành
        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        // Kiểm tra kết quả
        assertEquals(numberOfThreads, successCount.get(), "Tất cả user khác nhau nên nộp đơn thành công");
        
        // Kiểm tra trong database có đúng số lượng application
        long applicationCount = applicationRepository.count();
        assertEquals(numberOfThreads, applicationCount, "Database nên có " + numberOfThreads + " applications");
    }

    @Test
    void testConcurrentApplicationSubmission_DifferentJobs_AllShouldSucceed() throws Exception {
        int numberOfThreads = 3;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        // Tạo các job posting khác nhau
        JobPosting[] jobs = new JobPosting[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            jobs[i] = new JobPosting();
            jobs[i].setId(2000L + i);
            jobs[i].setTitle("Test Job " + i);
            jobs[i].setDescription("Test Description " + i);
            jobs[i].setStatus(JobStatus.ACTIVE);
            jobs[i].setJobType(JobType.FULL_TIME);
            jobs[i].setApplicationDeadline(LocalDateTime.now().plusDays(30));
            jobs[i].setPublishedAt(LocalDateTime.now());
            jobs[i].setCreatedBy(testUser);
            jobs[i].setApplicationsCount(0);
            jobPostingRepository.save(jobs[i]);
        }

        // Tạo các task song song với job khác nhau
        CompletableFuture<?>[] futures = new CompletableFuture[numberOfThreads];
        
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    latch.countDown();
                    latch.await(); // Đợi tất cả thread sẵn sàng

                    ApplicationRequest request = new ApplicationRequest();
                    request.setJobPostingId(jobs[index].getId());
                    request.setCoverLetter("Test cover letter for job " + index);
                    request.setResumeUrl("http://example.com/resume.pdf");

                    mockMvc.perform(post("/api/applications/my")
                            .with(user(testUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                            .andExpect(result -> {
                                int status = result.getResponse().getStatus();
                                if (status == 200) {
                                    successCount.incrementAndGet();
                                }
                            });
                } catch (Exception e) {
                    System.err.println("Thread error: " + e.getMessage());
                }
            }, executor);
        }

        // Đợi tất cả task hoàn thành
        CompletableFuture.allOf(futures).join();
        executor.shutdown();

        // Kiểm tra kết quả
        assertEquals(numberOfThreads, successCount.get(), "Tất cả job khác nhau nên nộp đơn thành công");
        
        // Kiểm tra trong database có đúng số lượng application
        long applicationCount = applicationRepository.count();
        assertEquals(numberOfThreads, applicationCount, "Database nên có " + numberOfThreads + " applications");
    }
}
