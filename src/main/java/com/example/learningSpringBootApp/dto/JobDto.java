package com.example.learningSpringBootApp.dto;

import java.time.LocalDateTime;

public class JobDto {

    private Long id;
    private String title;
    private String company;
    private String location;
    private String type;
    private String experienceLevel;
    private String salaryRange;
    private String description;
    private String requiredSkills;
    private boolean alreadyApplied;
    private int matchScore; // 0 - 100
    private String matchingSkills;
    private LocalDateTime createdAt;

    public JobDto() {
    }

    public JobDto(Long id, String title, String company, String location, String type,
                  String experienceLevel, String salaryRange, String description,
                  String requiredSkills, boolean alreadyApplied, int matchScore,
                  String matchingSkills, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.type = type;
        this.experienceLevel = experienceLevel;
        this.salaryRange = salaryRange;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.alreadyApplied = alreadyApplied;
        this.matchScore = matchScore;
        this.matchingSkills = matchingSkills;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public boolean isAlreadyApplied() {
        return alreadyApplied;
    }

    public void setAlreadyApplied(boolean alreadyApplied) {
        this.alreadyApplied = alreadyApplied;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public String getMatchingSkills() {
        return matchingSkills;
    }

    public void setMatchingSkills(String matchingSkills) {
        this.matchingSkills = matchingSkills;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
