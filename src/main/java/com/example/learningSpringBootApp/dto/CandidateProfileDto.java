package com.example.learningSpringBootApp.dto;

import java.time.LocalDateTime;

public class CandidateProfileDto {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String headline;
    private String summary;
    private String skills;
    private Integer experienceYears;
    private String resumeFileName;
    private String resumeText;
    private LocalDateTime updatedAt;

    public CandidateProfileDto() {
    }

    public CandidateProfileDto(Long id, Long userId, String fullName, String email,
                               String headline, String summary, String skills,
                               Integer experienceYears, String resumeFileName,
                               String resumeText, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.headline = headline;
        this.summary = summary;
        this.skills = skills;
        this.experienceYears = experienceYears;
        this.resumeFileName = resumeFileName;
        this.resumeText = resumeText;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getResumeFileName() {
        return resumeFileName;
    }

    public void setResumeFileName(String resumeFileName) {
        this.resumeFileName = resumeFileName;
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
