package com.recruitment.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.system.dto.request.ProfileRequest;
import com.recruitment.system.entity.Profile;
import com.recruitment.system.entity.User;
import com.recruitment.system.enums.UserRole;
import com.recruitment.system.enums.UserStatus;
import com.recruitment.system.repository.ProfileRepository;
import com.recruitment.system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public class ProfileControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User applicant;
    private User employer;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        applicant = new User();
        applicant.setEmail("applicant@example.com");
        applicant.setFirstName("Applicant");
        applicant.setLastName("User");
        applicant.setRole(UserRole.APPLICANT);
        applicant.setStatus(UserStatus.ACTIVE);
        applicant.setEmailVerified(true);
        userRepository.save(applicant);

        employer = new User();
        employer.setEmail("employer@example.com");
        employer.setFirstName("Employer");
        employer.setLastName("User");
        employer.setRole(UserRole.EMPLOYER);
        employer.setStatus(UserStatus.ACTIVE);
        employer.setEmailVerified(true);
        userRepository.save(employer);
    }

    @Test
    void getMyProfile_createsWhenMissing_happy() throws Exception {
        mockMvc.perform(get("/api/profiles/my").with(user(applicant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists());

        assertThat(profileRepository.existsByUserId(applicant.getId())).isTrue();
    }

    @Test
    void getMyProfile_existing_happy() throws Exception {
        Profile p = new Profile();
        p.setUser(applicant);
        p.setSummary("Hello");
        profileRepository.save(p);

        mockMvc.perform(get("/api/profiles/my").with(user(applicant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary").value("Hello"));
    }

    @Test
    void getMyProfile_unauthorized_401() throws Exception {
        mockMvc.perform(get("/api/profiles/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyProfile_forbidden_forNonApplicant_403() throws Exception {
        mockMvc.perform(get("/api/profiles/my").with(user(employer)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateMyProfile_happy_updatesText_preservesResumeUrl() throws Exception {
        // Tạo profile có resumeUrl sẵn
        Profile p = new Profile();
        p.setUser(applicant);
        p.setResumeUrl("/uploads/resumes/123.pdf");
        p.setSummary("old");
        profileRepository.save(p);

        ProfileRequest req = new ProfileRequest();
        req.setSummary("new summary");
        req.setSkills("Java, Spring");

        mockMvc.perform(put("/api/profiles/my")
                        .with(user(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary").value("new summary"));

        Profile updated = profileRepository.findByUserId(applicant.getId()).orElseThrow();
        assertThat(updated.getResumeUrl()).isEqualTo("/uploads/resumes/123.pdf");
        assertThat(updated.getSummary()).isEqualTo("new summary");
        assertThat(updated.getSkills()).isEqualTo("Java, Spring");
    }

    @Test
    void updateMyProfile_invalid_summaryTooLong_400() throws Exception {
        ProfileRequest req = new ProfileRequest();
        req.setSummary("A".repeat(2001));

        mockMvc.perform(put("/api/profiles/my")
                        .with(user(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Tóm tắt không được vượt quá")));
    }

    @Test
    void updateMyProfile_forbidden_forNonApplicant_403() throws Exception {
        ProfileRequest req = new ProfileRequest();
        req.setSummary("ok");

        mockMvc.perform(put("/api/profiles/my")
                        .with(user(employer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateMyProfile_createsWhenMissing_happy() throws Exception {
        ProfileRequest req = new ProfileRequest();
        req.setSummary("first time");

        mockMvc.perform(put("/api/profiles/my")
                        .with(user(applicant))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary").value("first time"));

        assertThat(profileRepository.existsByUserId(applicant.getId())).isTrue();
    }
}


