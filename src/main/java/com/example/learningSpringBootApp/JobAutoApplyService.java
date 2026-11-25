package com.example.learningSpringBootApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.learningSpringBootApp.dto.AutoApplyResultDto;
import com.example.learningSpringBootApp.dto.JobApplicationDto;
import com.example.learningSpringBootApp.entity.CandidateProfileEntity;
import com.example.learningSpringBootApp.entity.JobApplicationEntity;
import com.example.learningSpringBootApp.entity.JobEntity;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.CandidateProfileRepository;
import com.example.learningSpringBootApp.repository.JobApplicationRepository;
import com.example.learningSpringBootApp.repository.JobRepository;
import com.example.learningSpringBootApp.repository.UserRepository;

@Service
public class JobAutoApplyService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;

    public JobAutoApplyService(JobRepository jobRepository,
                               JobApplicationRepository jobApplicationRepository,
                               CandidateProfileRepository candidateProfileRepository,
                               UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public JobApplicationDto applySingleJob(Long jobId, Long userId) {
        JobEntity job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job with ID " + jobId + " not found."));

        if (!job.isActive()) {
            throw new IllegalStateException("This job opening is no longer active.");
        }

        if (jobApplicationRepository.existsByJobIdAndUserId(jobId, userId)) {
            throw new IllegalStateException("You have already submitted an application for this position.");
        }

        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID " + userId));

        CandidateProfileEntity profile = candidateProfileRepository.findByUserId(userId)
            .orElseGet(() -> {
                CandidateProfileEntity fallback = new CandidateProfileEntity(userId, user.getName(), user.getEmail());
                return candidateProfileRepository.save(fallback);
            });

        JobService.MatchResult match = JobService.computeMatch(job, profile);

        JobApplicationEntity application = new JobApplicationEntity(
            job.getId(),
            userId,
            profile.getFullName() != null ? profile.getFullName() : user.getName(),
            profile.getEmail() != null ? profile.getEmail() : user.getEmail(),
            match.score,
            match.matchingSkills,
            false // manual apply
        );

        JobApplicationEntity saved = jobApplicationRepository.save(application);
        return toDto(saved, job);
    }

    @Transactional
    public AutoApplyResultDto autoApplyAllMatchingJobs(Long userId) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID " + userId));

        CandidateProfileEntity profile = candidateProfileRepository.findByUserId(userId)
            .orElseGet(() -> {
                CandidateProfileEntity fallback = new CandidateProfileEntity(userId, user.getName(), user.getEmail());
                fallback.setHeadline("Software Developer");
                fallback.setSkills("Java, Spring Boot, SQL, Git");
                return candidateProfileRepository.save(fallback);
            });

        boolean hasSkills = profile.getSkills() != null && !profile.getSkills().isBlank();
        boolean hasResume = profile.getResumeText() != null && !profile.getResumeText().isBlank();

        if (!hasSkills && !hasResume) {
            throw new IllegalStateException(
                "Please add skills or upload your resume in the 'My Profile' tab before running Auto-Apply."
            );
        }

        List<JobEntity> activeJobs = jobRepository.findByActiveTrueOrderByIdDesc();
        int evaluated = activeJobs.size();
        int appliedCount = 0;
        int alreadyAppliedCount = 0;
        int skippedLowMatchCount = 0;
        List<JobApplicationDto> newlyAppliedList = new ArrayList<>();

        for (JobEntity job : activeJobs) {
            if (jobApplicationRepository.existsByJobIdAndUserId(job.getId(), userId)) {
                alreadyAppliedCount++;
                continue;
            }

            JobService.MatchResult match = JobService.computeMatch(job, profile);

            // Suitability condition:
            // Either match score >= 35%, or candidate matches at least 1 significant required skill
            int matchedSkillCount = match.matchingSkills.isBlank() ? 0 : match.matchingSkills.split(",").length;
            boolean isSuitable = match.score >= 35 || matchedSkillCount >= 1;

            if (isSuitable) {
                JobApplicationEntity application = new JobApplicationEntity(
                    job.getId(),
                    userId,
                    profile.getFullName() != null ? profile.getFullName() : user.getName(),
                    profile.getEmail() != null ? profile.getEmail() : user.getEmail(),
                    match.score,
                    match.matchingSkills,
                    true // autoApplied
                );

                JobApplicationEntity saved = jobApplicationRepository.save(application);
                newlyAppliedList.add(toDto(saved, job));
                appliedCount++;
            } else {
                skippedLowMatchCount++;
            }
        }

        String msg;
        if (appliedCount > 0) {
            msg = String.format("Successfully auto-applied to %d suitable jobs matching your profile & resume!", appliedCount);
        } else if (alreadyAppliedCount > 0 && activeJobs.size() == alreadyAppliedCount) {
            msg = "You have already applied to all currently active positions.";
        } else {
            msg = "Evaluated all open positions. No new jobs reached the suitability threshold (35%+ skill match). Consider updating your skills or resume to match more opportunities.";
        }

        return new AutoApplyResultDto(
            evaluated,
            appliedCount,
            alreadyAppliedCount,
            skippedLowMatchCount,
            newlyAppliedList,
            msg
        );
    }

    public List<JobApplicationDto> getMyApplications(Long userId) {
        List<JobApplicationEntity> applications = jobApplicationRepository.findByUserIdOrderByAppliedAtDesc(userId);
        if (applications.isEmpty()) {
            return List.of();
        }

        List<Long> jobIds = applications.stream().map(JobApplicationEntity::getJobId).distinct().toList();
        Map<Long, JobEntity> jobMap = jobRepository.findAllById(jobIds).stream()
            .collect(Collectors.toMap(JobEntity::getId, Function.identity()));

        return applications.stream()
            .map(app -> toDto(app, jobMap.get(app.getJobId())))
            .collect(Collectors.toList());
    }

    private JobApplicationDto toDto(JobApplicationEntity app, JobEntity job) {
        String jobTitle = (job != null) ? job.getTitle() : "Unknown Position";
        String company = (job != null) ? job.getCompany() : "Unknown Company";
        String location = (job != null) ? job.getLocation() : "N/A";
        String type = (job != null) ? job.getType() : "N/A";
        String salaryRange = (job != null) ? job.getSalaryRange() : "Competitive";

        return new JobApplicationDto(
            app.getId(),
            app.getJobId(),
            jobTitle,
            company,
            location,
            type,
            salaryRange,
            app.getMatchScore(),
            app.getMatchingSkills(),
            app.getStatus(),
            app.isAutoApplied(),
            app.getAppliedAt()
        );
    }
}
