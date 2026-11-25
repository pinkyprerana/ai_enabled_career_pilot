package com.example.learningSpringBootApp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import com.example.learningSpringBootApp.dto.AutoApplyResultDto;
import com.example.learningSpringBootApp.dto.CandidateProfileDto;
import com.example.learningSpringBootApp.dto.JobApplicationDto;
import com.example.learningSpringBootApp.dto.JobDto;
import com.example.learningSpringBootApp.entity.JobEntity;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.CandidateProfileRepository;
import com.example.learningSpringBootApp.repository.JobApplicationRepository;
import com.example.learningSpringBootApp.repository.JobRepository;
import com.example.learningSpringBootApp.repository.UserRepository;

@SpringBootTest
@Transactional
public class JobControllerE2ETests {

    @Autowired
    private JobController jobController;

    @Autowired
    private ProfileController profileController;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private JobService jobService;

    private UserEntity candidate;
    private Authentication auth;

    @BeforeEach
    void setUp() {
        jobApplicationRepository.deleteAll();
        candidateProfileRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        candidate = userRepository.save(new UserEntity("Bob Dev", "bob@example.com", "secret123", "ROLE_USER"));
        jobService.seedInitialJobs();

        auth = new UsernamePasswordAuthenticationToken(
            candidate.getEmail(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void testGetJobsEndpoint() {
        ResponseEntity<List<JobDto>> response = jobController.getJobs(null, null, null, auth);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(8, response.getBody().size());
    }

    @Test
    void testSearchJobsByKeywordAndType() {
        ResponseEntity<List<JobDto>> response = jobController.getJobs("Google", "Full-time", null, auth);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals("Google Cloud", response.getBody().get(0).getCompany());
    }

    @Test
    void testProfileLifecycleAndAutoApplyEndToEnd() {
        // 1. Get initial profile
        ResponseEntity<CandidateProfileDto> profileRes = profileController.getProfile(auth);
        assertEquals(HttpStatus.OK, profileRes.getStatusCode());
        assertNotNull(profileRes.getBody());

        // 2. Update Profile with technical skills
        CandidateProfileDto updateReq = new CandidateProfileDto();
        updateReq.setFullName("Bob Senior Engineer");
        updateReq.setHeadline("Lead Full Stack Java & Cloud Architect");
        updateReq.setExperienceYears(5);
        updateReq.setSkills("Java, Spring Boot, Microservices, React, Docker, Kubernetes, SQL, AWS");
        updateReq.setSummary("Building highly available enterprise applications");

        ResponseEntity<CandidateProfileDto> updatedRes = profileController.updateProfile(updateReq, auth);
        assertEquals(HttpStatus.OK, updatedRes.getStatusCode());
        assertEquals("Bob Senior Engineer", updatedRes.getBody().getFullName());

        // 3. Save Resume text
        ResponseEntity<CandidateProfileDto> resumeRes = profileController.saveResumeText(
            Map.of("resumeText", "Bob has strong skills in Java, Spring Boot, React, and AWS cloud architecture."),
            auth
        );
        assertEquals(HttpStatus.OK, resumeRes.getStatusCode());

        // 4. Trigger Auto-Apply
        ResponseEntity<AutoApplyResultDto> autoApplyRes = jobController.autoApplyJobs(auth);
        assertEquals(HttpStatus.OK, autoApplyRes.getStatusCode());
        AutoApplyResultDto autoResult = autoApplyRes.getBody();
        assertNotNull(autoResult);
        assertTrue(autoResult.getAppliedCount() > 0, "Candidate with Java/React/AWS should auto-apply to matching jobs");

        // 5. Check My Applications
        ResponseEntity<List<JobApplicationDto>> appsRes = jobController.getMyApplications(auth);
        assertEquals(HttpStatus.OK, appsRes.getStatusCode());
        List<JobApplicationDto> apps = appsRes.getBody();
        assertNotNull(apps);
        assertEquals(autoResult.getAppliedCount(), apps.size());
        assertTrue(apps.stream().allMatch(JobApplicationDto::isAutoApplied));

        // 6. Test Manual Apply to remaining job if any
        List<JobEntity> allJobs = jobRepository.findAll();
        for (JobEntity j : allJobs) {
            boolean already = apps.stream().anyMatch(a -> a.getJobId().equals(j.getId()));
            if (!already) {
                ResponseEntity<JobApplicationDto> manualApp = jobController.applyToJob(j.getId(), auth);
                assertEquals(HttpStatus.OK, manualApp.getStatusCode());
                assertFalse(manualApp.getBody().isAutoApplied());
                break;
            }
        }
    }
}
