package com.example.learningSpringBootApp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.learningSpringBootApp.dto.CandidateProfileDto;
import com.example.learningSpringBootApp.entity.UserEntity;
import com.example.learningSpringBootApp.repository.UserRepository;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final CandidateProfileService profileService;
    private final UserRepository userRepository;

    public ProfileController(CandidateProfileService profileService,
                             UserRepository userRepository) {
        this.profileService = profileService;
        this.userRepository = userRepository;
    }

    private UserEntity getAuthenticatedUser(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    @GetMapping
    public ResponseEntity<CandidateProfileDto> getProfile(Authentication auth) {
        UserEntity user = getAuthenticatedUser(auth);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        CandidateProfileDto profile = profileService.getProfile(user.getId(), user.getEmail(), user.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<CandidateProfileDto> updateProfile(@RequestBody CandidateProfileDto dto, Authentication auth) {
        UserEntity user = getAuthenticatedUser(auth);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        dto.setUserId(user.getId());
        CandidateProfileDto updated = profileService.updateProfile(user.getId(), dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/resume")
    public ResponseEntity<CandidateProfileDto> uploadResume(
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam(name = "resumeText", required = false) String resumeText,
            Authentication auth) throws IOException {
        UserEntity user = getAuthenticatedUser(auth);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        String fileName = "resume.txt";
        String content = resumeText;

        if (file != null && !file.isEmpty()) {
            fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "uploaded_resume.txt";
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        }

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        CandidateProfileDto updated = profileService.saveResume(user.getId(), fileName, content);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/resume-text")
    public ResponseEntity<CandidateProfileDto> saveResumeText(@RequestBody Map<String, String> payload, Authentication auth) {
        UserEntity user = getAuthenticatedUser(auth);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        String text = payload.get("resumeText");
        String fileName = payload.getOrDefault("fileName", "pasted_resume.txt");

        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        CandidateProfileDto updated = profileService.saveResume(user.getId(), fileName, text);
        return ResponseEntity.ok(updated);
    }
}
