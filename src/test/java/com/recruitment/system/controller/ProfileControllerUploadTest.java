package com.recruitment.system.controller;

import com.recruitment.system.entity.User;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.enums.UserStatus;
import com.recruitment.system.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
public class ProfileControllerUploadTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    // Repository không cần dùng trong các test validation này

    @MockBean
    private StorageService storageService;

    private MockMvc mockMvc;
    private User applicantUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        applicantUser = new User();
        applicantUser.setId(777L);
        applicantUser.setEmail("applicant@example.com");
        applicantUser.setFirstName("Applicant");
        applicantUser.setLastName("User");
        applicantUser.setRole(UserRole.APPLICANT);
        applicantUser.setStatus(UserStatus.ACTIVE);
        applicantUser.setEmailVerified(true);
    }

    @Test
    void uploadResume_wrongMime_should400() throws Exception {
        MockMultipartFile wrong = new MockMultipartFile(
                "file",
                "cv.txt",
                "text/plain",
                "hello".getBytes()
        );

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/profiles/my/resume")
                        .file(wrong)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(applicantUser))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void uploadResume_oversize_should400() throws Exception {
        int size = 5 * 1024 * 1024 + 1; // > 5MB
        byte[] bytes = new byte[size];
        bytes[0] = '%'; bytes[1] = 'P'; bytes[2] = 'D'; bytes[3] = 'F'; bytes[4] = '-';

        MockMultipartFile bigPdf = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                bytes
        );

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/profiles/my/resume")
                        .file(bigPdf)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(applicantUser))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void uploadResume_noFile_should400() throws Exception {
        // Không gửi nội dung file (empty part) để trigger kiểm tra isEmpty()
        MockMultipartFile empty = new MockMultipartFile(
                "file",
                "cv.pdf",
                "application/pdf",
                new byte[0]
        );

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/api/profiles/my/resume")
                        .file(empty)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(applicantUser))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}


