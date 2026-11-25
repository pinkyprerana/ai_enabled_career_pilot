package com.example.learningSpringBootApp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.learningSpringBootApp.dto.AutoApplyResultDto;
import com.example.learningSpringBootApp.dto.CandidateProfileDto;
import com.example.learningSpringBootApp.dto.JobApplicationDto;
import com.example.learningSpringBootApp.dto.JobDto;
import com.example.learningSpringBootApp.entity.CandidateProfileEntity;
import com.example.learningSpringBootApp.entity.JobEntity;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.CandidateProfileRepository;
import com.example.learningSpringBootApp.repository.JobApplicationRepository;
import com.example.learningSpringBootApp.repository.JobRepository;
import com.example.learningSpringBootApp.repository.UserRepository;

@SpringBootTest
@Transactional
public class JobAndAutoApplyTests {

    @Autowired
    private JobService jobService;

    @Autowired
    private CandidateProfileService candidateProfileService;

    @Autowired
    private JobAutoApplyService jobAutoApplyService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        jobApplicationRepository.deleteAll();
        candidateProfileRepository.deleteAll();
        jobRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(new UserEntity("Alice Dev", "alice@test.com", "pass123", "ROLE_USER"));
        jobService.seedInitialJobs();
    }

    @Test
    void testSeedJobsCreated() {
        List<JobEntity> jobs = jobRepository.findAll();
        assertTrue(jobs.size() >= 8, "Initial jobs should be seeded");
    }

    @Test
    void testComputeMatchHighMatch() {
        JobEntity job = new JobEntity("Backend Engineer", "TechCorp", "Remote", "Full-time", "Mid", "$120k", "desc", "Java, Spring Boot, SQL, Docker");
        CandidateProfileEntity profile = new CandidateProfileEntity(testUser.getId(), "Alice", "alice@test.com");
        profile.setSkills("Java, Spring Boot, SQL, Docker, Kubernetes");

        JobService.MatchResult match = JobService.computeMatch(job, profile);
        assertEquals(100, match.score);
        assertTrue(match.matchingSkills.contains("Java"));
        assertTrue(match.matchingSkills.contains("Spring Boot"));
    }

    @Test
    void testComputeMatchPartialMatch() {
        JobEntity job = new JobEntity("Frontend React Dev", "WebCorp", "Remote", "Full-time", "Mid", "$110k", "desc", "React, TypeScript, Redux, Tailwind CSS");
        CandidateProfileEntity profile = new CandidateProfileEntity(testUser.getId(), "Alice", "alice@test.com");
        profile.setSkills("Java, Spring Boot, React");

        JobService.MatchResult match = JobService.computeMatch(job, profile);
        assertEquals(25, match.score); // 1 out of 4 matches
        assertEquals("React", match.matchingSkills);
    }

    @Test
    void testResumeUploadAndSkillAutoExtraction() {
        String resumeText = "Experienced developer with 4 years building systems using Java, Spring Boot, Microservices, and Docker on AWS cloud.";
        CandidateProfileDto updated = candidateProfileService.saveResume(testUser.getId(), "my_resume.txt", resumeText);

        assertNotNull(updated);
        assertNotNull(updated.getSkills());
        assertTrue(updated.getSkills().contains("Java"));
        assertTrue(updated.getSkills().contains("Spring Boot"));
        assertTrue(updated.getSkills().contains("Docker"));
        assertTrue(updated.getSkills().contains("AWS"));
    }

    @Test
    void testSingleJobApplyAndDuplicatePrevention() {
        List<JobEntity> jobs = jobRepository.findAll();
        JobEntity targetJob = jobs.get(0);

        JobApplicationDto app = jobAutoApplyService.applySingleJob(targetJob.getId(), testUser.getId());
        assertNotNull(app);
        assertEquals(targetJob.getId(), app.getJobId());
        assertEquals("APPLIED", app.getStatus());
        assertFalse(app.isAutoApplied());

        // Attempt duplicate application
        assertThrows(IllegalStateException.class, () -> {
            jobAutoApplyService.applySingleJob(targetJob.getId(), testUser.getId());
        });
    }

    @Test
    void testAutoApplyAllMatchingJobs() {
        // Prepare profile with strong Java/Spring skills
        CandidateProfileDto profile = candidateProfileService.getProfile(testUser.getId(), testUser.getEmail(), testUser.getName());
        profile.setSkills("Java, Spring Boot, Microservices, SQL, Docker, AWS");
        candidateProfileService.updateProfile(testUser.getId(), profile);

        AutoApplyResultDto result = jobAutoApplyService.autoApplyAllMatchingJobs(testUser.getId());
        assertNotNull(result);
        assertTrue(result.getAppliedCount() > 0, "Should auto-apply to matching Java / Spring Boot jobs");

        List<JobApplicationDto> myApps = jobAutoApplyService.getMyApplications(testUser.getId());
        assertEquals(result.getAppliedCount(), myApps.size());
        assertTrue(myApps.stream().allMatch(JobApplicationDto::isAutoApplied));

        // Running auto-apply again should not duplicate applications
        AutoApplyResultDto secondRun = jobAutoApplyService.autoApplyAllMatchingJobs(testUser.getId());
        assertEquals(0, secondRun.getAppliedCount(), "Should not re-apply to already applied jobs");
        assertTrue(secondRun.getAlreadyAppliedCount() > 0);
    }

    @Test
    void testFilterJobsByKeywordAndType() {
        List<JobDto> fullTimeJobs = jobService.listJobs(testUser.getId(), "Java", "Full-time", null);
        assertFalse(fullTimeJobs.isEmpty());
        assertTrue(fullTimeJobs.stream().allMatch(j -> j.getType().equalsIgnoreCase("Full-time")));
    }
}
