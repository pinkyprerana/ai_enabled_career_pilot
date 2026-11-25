package com.example.learningSpringBootApp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.learningSpringBootApp.dto.CandidateProfileDto;
import com.example.learningSpringBootApp.entity.CandidateProfileEntity;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.CandidateProfileRepository;
import com.example.learningSpringBootApp.repository.UserRepository;

@Service
public class CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final UserRepository userRepository;

    private static final List<String> COMMON_SKILLS_DICTIONARY = List.of(
        "Java", "Spring Boot", "Microservices", "REST API", "Hibernate", "JPA",
        "React", "Angular", "Vue.js", "TypeScript", "JavaScript", "HTML5", "CSS3", "Tailwind CSS", "Redux", "Next.js",
        "Python", "Django", "FastAPI", "Go", "C++", "C#", ".NET",
        "SQL", "MySQL", "PostgreSQL", "MongoDB", "Redis", "Kafka", "RabbitMQ", "Elasticsearch",
        "Docker", "Kubernetes", "AWS", "Azure", "Google Cloud", "CI/CD", "Terraform", "Linux", "Git",
        "GraphQL", "OAuth2", "JWT", "OOP", "Distributed Systems", "Android", "Kotlin", "Swift"
    );

    public CandidateProfileService(CandidateProfileRepository candidateProfileRepository,
                                  UserRepository userRepository) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.userRepository = userRepository;
    }

    public CandidateProfileDto getProfile(Long userId, String email, String name) {
        CandidateProfileEntity profile = candidateProfileRepository.findByUserId(userId)
            .orElseGet(() -> {
                CandidateProfileEntity newProfile = new CandidateProfileEntity(userId, name, email);
                newProfile.setHeadline("Software Engineer");
                newProfile.setSkills("Java, Spring Boot, REST API, SQL, Git");
                newProfile.setExperienceYears(2);
                newProfile.setSummary("Motivated software developer passionate about building scalable, secure backend and full stack web applications.");
                return candidateProfileRepository.save(newProfile);
            });

        return toDto(profile);
    }

    public CandidateProfileDto updateProfile(Long userId, CandidateProfileDto dto) {
        CandidateProfileEntity profile = candidateProfileRepository.findByUserId(userId)
            .orElseGet(() -> new CandidateProfileEntity(userId, dto.getFullName(), dto.getEmail()));

        if (dto.getFullName() != null) profile.setFullName(dto.getFullName().trim());
        if (dto.getHeadline() != null) profile.setHeadline(dto.getHeadline().trim());
        if (dto.getSummary() != null) profile.setSummary(dto.getSummary().trim());
        if (dto.getSkills() != null) profile.setSkills(cleanAndFormatSkills(dto.getSkills()));
        if (dto.getExperienceYears() != null) profile.setExperienceYears(dto.getExperienceYears());

        profile.setUpdatedAt(LocalDateTime.now());
        CandidateProfileEntity saved = candidateProfileRepository.save(profile);
        return toDto(saved);
    }

    public CandidateProfileDto saveResume(Long userId, String fileName, String resumeContent) {
        CandidateProfileEntity profile = candidateProfileRepository.findByUserId(userId)
            .orElseGet(() -> {
                UserEntity user = userRepository.findById(userId).orElse(null);
                String name = user != null ? user.getName() : "Candidate";
                String email = user != null ? user.getEmail() : "";
                return new CandidateProfileEntity(userId, name, email);
            });

        profile.setResumeFileName(fileName != null ? fileName : "resume.txt");
        profile.setResumeText(resumeContent);
        profile.setUpdatedAt(LocalDateTime.now());

        // Auto-extract tech skills detected in resume content and merge with existing skills
        if (resumeContent != null && !resumeContent.isBlank()) {
            Set<String> existingSkills = parseSkillsToSet(profile.getSkills());
            String lowerContent = resumeContent.toLowerCase();

            for (String dictSkill : COMMON_SKILLS_DICTIONARY) {
                String dictLower = dictSkill.toLowerCase();
                boolean matched = false;
                if (dictSkill.length() <= 3) {
                    matched = resumeContent.matches("(?i).*\\b" + java.util.regex.Pattern.quote(dictSkill) + "\\b.*");
                } else {
                    matched = lowerContent.contains(dictLower);
                }

                if (matched) {
                    existingSkills.add(dictSkill);
                }
            }

            profile.setSkills(String.join(", ", existingSkills));
        }

        CandidateProfileEntity saved = candidateProfileRepository.save(profile);
        return toDto(saved);
    }

    private Set<String> parseSkillsToSet(String skills) {
        if (skills == null || skills.isBlank()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(skills.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String cleanAndFormatSkills(String rawSkills) {
        if (rawSkills == null || rawSkills.isBlank()) return "";
        return Arrays.stream(rawSkills.split("[,;\\n]"))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .distinct()
            .collect(Collectors.joining(", "));
    }

    public CandidateProfileDto toDto(CandidateProfileEntity entity) {
        return new CandidateProfileDto(
            entity.getId(),
            entity.getUserId(),
            entity.getFullName(),
            entity.getEmail(),
            entity.getHeadline(),
            entity.getSummary(),
            entity.getSkills(),
            entity.getExperienceYears(),
            entity.getResumeFileName(),
            entity.getResumeText(),
            entity.getUpdatedAt()
        );
    }
}
