package com.example.learningSpringBootApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.learningSpringBootApp.dto.JobDto;
import com.example.learningSpringBootApp.entity.CandidateProfileEntity;
import com.example.learningSpringBootApp.entity.JobEntity;
import com.example.learningSpringBootApp.repository.CandidateProfileRepository;
import com.example.learningSpringBootApp.repository.JobApplicationRepository;
import com.example.learningSpringBootApp.repository.JobRepository;

import jakarta.annotation.PostConstruct;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final CandidateProfileRepository candidateProfileRepository;

    public JobService(JobRepository jobRepository,
                      JobApplicationRepository jobApplicationRepository,
                      CandidateProfileRepository candidateProfileRepository) {
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.candidateProfileRepository = candidateProfileRepository;
    }

    @PostConstruct
    public void seedInitialJobs() {
        if (jobRepository.count() == 0) {
            List<JobEntity> seedJobs = List.of(
                new JobEntity(
                    "Senior Full Stack Java Engineer",
                    "Google Cloud",
                    "Sunnyvale, CA (Remote)",
                    "Full-time",
                    "Senior (5+ yrs)",
                    "$160,000 - $210,000",
                    "Design and build scalable microservices and reactive user interfaces for Google Cloud Console. Work with high-concurrency systems, Kubernetes orchestration, and Spring Boot enterprise architectures.",
                    "Java, Spring Boot, Microservices, React, Docker, Kubernetes, Cloud, SQL"
                ),
                new JobEntity(
                    "Backend Spring Boot Developer",
                    "Amazon Web Services",
                    "Seattle, WA (Hybrid)",
                    "Full-time",
                    "Mid-level (3+ yrs)",
                    "$135,000 - $175,000",
                    "Develop robust RESTful APIs and distributed event-driven systems using Spring Boot, Kafka, and DynamoDB. Optimize database transactions and implement rock-solid security protocols.",
                    "Java, Spring Boot, REST API, Microservices, Kafka, Redis, SQL, AWS"
                ),
                new JobEntity(
                    "Frontend React & TypeScript Specialist",
                    "Netflix",
                    "Los Gatos, CA (Remote)",
                    "Full-time",
                    "Mid-Senior (4+ yrs)",
                    "$145,000 - $190,000",
                    "Create intuitive, high-performance UI components for Netflix internal tool suites and creator portals. Deep experience with modern React, TypeScript, state management, and responsive CSS is essential.",
                    "React, TypeScript, JavaScript, Redux, HTML5, CSS3, Tailwind CSS, REST API"
                ),
                new JobEntity(
                    "DevOps & Cloud Infrastructure Engineer",
                    "Microsoft Azure",
                    "Redmond, WA (Hybrid)",
                    "Full-time",
                    "Mid-level (3+ yrs)",
                    "$130,000 - $170,000",
                    "Maintain continuous integration and deployment pipelines (CI/CD), automate multi-region infrastructure provisioning using Terraform, and monitor containerized Kubernetes clusters.",
                    "Docker, Kubernetes, AWS, CI/CD, Terraform, Linux, Git, Cloud"
                ),
                new JobEntity(
                    "Full Stack Software Engineer",
                    "Meta",
                    "Menlo Park, CA (Remote)",
                    "Full-time",
                    "Mid-level (2+ yrs)",
                    "$140,000 - $185,000",
                    "Collaborate across cross-functional teams to deliver end-to-end features connecting React frontends with Java Spring Boot and GraphQL services. Passionate about clean code, unit testing, and scalable architecture.",
                    "Java, Spring Boot, React, JavaScript, GraphQL, PostgreSQL, Git, Docker"
                ),
                new JobEntity(
                    "Junior Java / Spring Boot Developer",
                    "Accenture",
                    "Austin, TX (On-site)",
                    "Full-time",
                    "Entry-level (0-2 yrs)",
                    "$75,000 - $95,000",
                    "Great opportunity for early-career software engineers! Learn from experienced architects while building enterprise Spring Boot microservices, writing unit tests, and designing relational databases.",
                    "Java, Spring Boot, SQL, Git, OOP, REST API"
                ),
                new JobEntity(
                    "Staff Distributed Systems Architect",
                    "Uber",
                    "San Francisco, CA (Hybrid)",
                    "Full-time",
                    "Lead / Staff (8+ yrs)",
                    "$210,000 - $280,000",
                    "Lead architecture and technical strategy for mission-critical dispatch and routing engines. Deep mastery of low-latency Java concurrency, Kafka event pipelines, and cloud scalability is required.",
                    "Java, Spring Boot, Microservices, Distributed Systems, Kafka, Kubernetes, Redis, Cloud"
                ),
                new JobEntity(
                    "Mobile Application Engineer (Android)",
                    "Spotify",
                    "New York, NY (Remote)",
                    "Full-time",
                    "Mid-level (3+ yrs)",
                    "$130,000 - $165,000",
                    "Craft smooth, delightful music streaming interfaces on Android. Work with Kotlin, Jetpack Compose, asynchronous coroutines, and REST / GraphQL networking.",
                    "Android, Kotlin, Java, REST API, Git, Mobile"
                )
            );
            jobRepository.saveAll(seedJobs);
        }
    }

    public List<JobDto> listJobs(Long userId, String query, String type, String location) {
        List<JobEntity> jobs = jobRepository.filterJobs(query, type, location);

        Optional<CandidateProfileEntity> profileOpt = (userId != null)
            ? candidateProfileRepository.findByUserId(userId)
            : Optional.empty();

        CandidateProfileEntity profile = profileOpt.orElse(null);

        return jobs.stream()
            .map(job -> toDto(job, userId, profile))
            .collect(Collectors.toList());
    }

    public JobDto getJobById(Long jobId, Long userId) {
        JobEntity job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job with ID " + jobId + " not found."));

        Optional<CandidateProfileEntity> profileOpt = (userId != null)
            ? candidateProfileRepository.findByUserId(userId)
            : Optional.empty();

        return toDto(job, userId, profileOpt.orElse(null));
    }

    public JobDto toDto(JobEntity job, Long userId, CandidateProfileEntity profile) {
        boolean alreadyApplied = false;
        if (userId != null) {
            alreadyApplied = jobApplicationRepository.existsByJobIdAndUserId(job.getId(), userId);
        }

        int matchScore = 0;
        String matchingSkills = "";

        if (profile != null) {
            MatchResult match = computeMatch(job, profile);
            matchScore = match.score;
            matchingSkills = match.matchingSkills;
        }

        return new JobDto(
            job.getId(),
            job.getTitle(),
            job.getCompany(),
            job.getLocation(),
            job.getType(),
            job.getExperienceLevel(),
            job.getSalaryRange(),
            job.getDescription(),
            job.getRequiredSkills(),
            alreadyApplied,
            matchScore,
            matchingSkills,
            job.getCreatedAt()
        );
    }

    public static class MatchResult {
        public final int score;
        public final String matchingSkills;

        public MatchResult(int score, String matchingSkills) {
            this.score = score;
            this.matchingSkills = matchingSkills;
        }
    }

    public static MatchResult computeMatch(JobEntity job, CandidateProfileEntity profile) {
        if (job == null || profile == null) {
            return new MatchResult(0, "");
        }

        String jobSkillsRaw = job.getRequiredSkills() != null ? job.getRequiredSkills() : "";
        List<String> requiredSkills = Arrays.stream(jobSkillsRaw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());

        if (requiredSkills.isEmpty()) {
            return new MatchResult(100, "");
        }

        // Candidate search pool: skills + headline + summary + resumeText
        StringBuilder candidatePool = new StringBuilder();
        if (profile.getSkills() != null) candidatePool.append(" ").append(profile.getSkills());
        if (profile.getHeadline() != null) candidatePool.append(" ").append(profile.getHeadline());
        if (profile.getSummary() != null) candidatePool.append(" ").append(profile.getSummary());
        if (profile.getResumeText() != null) candidatePool.append(" ").append(profile.getResumeText());

        String poolLower = candidatePool.toString().toLowerCase();

        List<String> matched = new ArrayList<>();
        for (String req : requiredSkills) {
            String reqLower = req.toLowerCase();
            // Match whole word / phrase
            if (containsSkill(poolLower, reqLower)) {
                matched.add(req);
            }
        }

        int score = (int) Math.round(((double) matched.size() / requiredSkills.size()) * 100.0);
        if (score > 100) score = 100;

        return new MatchResult(score, String.join(", ", matched));
    }

    private static boolean containsSkill(String text, String skill) {
        if (skill.length() <= 3) {
            // For short acronyms (AWS, SQL, Git, OOP), enforce boundary check
            return text.matches(".*\\b" + java.util.regex.Pattern.quote(skill) + "\\b.*");
        }
        return text.contains(skill);
    }
}
