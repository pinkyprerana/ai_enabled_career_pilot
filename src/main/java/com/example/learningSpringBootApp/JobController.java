package com.example.learningSpringBootApp;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.learningSpringBootApp.dto.AutoApplyResultDto;
import com.example.learningSpringBootApp.dto.JobDto;
import com.example.learningSpringBootApp.dto.JobApplicationDto;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.UserRepository;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final JobAutoApplyService jobAutoApplyService;
    private final UserRepository userRepository;

    public JobController(JobService jobService,
                         JobAutoApplyService jobAutoApplyService,
                         UserRepository userRepository) {
        this.jobService = jobService;
        this.jobAutoApplyService = jobAutoApplyService;
        this.userRepository = userRepository;
    }

    private Long getUserIdFromAuth(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName())
            .map(UserEntity::getId)
            .orElse(null);
    }

    @GetMapping
    public ResponseEntity<List<JobDto>> getJobs(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "location", required = false) String location,
            Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        List<JobDto> jobs = jobService.listJobs(userId, query, type, location);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobById(@PathVariable("id") Long id, Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        JobDto job = jobService.getJobById(id, userId);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<JobApplicationDto> applyToJob(@PathVariable("id") Long id, Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        JobApplicationDto result = jobAutoApplyService.applySingleJob(id, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/auto-apply")
    public ResponseEntity<AutoApplyResultDto> autoApplyJobs(Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        AutoApplyResultDto result = jobAutoApplyService.autoApplyAllMatchingJobs(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-applications")
    public ResponseEntity<List<JobApplicationDto>> getMyApplications(Authentication auth) {
        Long userId = getUserIdFromAuth(auth);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<JobApplicationDto> applications = jobAutoApplyService.getMyApplications(userId);
        return ResponseEntity.ok(applications);
    }
}
