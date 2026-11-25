package com.example.learningSpringBootApp.dto;

import java.time.LocalDateTime;

public class JobApplicationDto {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String location;
    private String type;
    private String salaryRange;
    private int matchScore;
    private String matchingSkills;
    private String status;
    private boolean autoApplied;
    private LocalDateTime appliedAt;

    public JobApplicationDto() {
    }

    public JobApplicationDto(Long id, Long jobId, String jobTitle, String company,
                             String location, String type, String salaryRange,
                             int matchScore, String matchingSkills, String status,
                             boolean autoApplied, LocalDateTime appliedAt) {
        this.id = id;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.company = company;
        this.location = location;
        this.type = type;
        this.salaryRange = salaryRange;
        this.matchScore = matchScore;
        this.matchingSkills = matchingSkills;
        this.status = status;
        this.autoApplied = autoApplied;
        this.appliedAt = appliedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
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

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAutoApplied() {
        return autoApplied;
    }

    public void setAutoApplied(boolean autoApplied) {
        this.autoApplied = autoApplied;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
