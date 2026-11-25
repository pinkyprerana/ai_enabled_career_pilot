package com.example.learningSpringBootApp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "job_applications", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"job_id", "user_id"})
})
public class JobApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String candidateName;

    private String candidateEmail;

    private int matchScore; // 0 - 100

    @Column(length = 1000)
    private String matchingSkills;

    @Column(nullable = false)
    private String status = "APPLIED"; // APPLIED, UNDER_REVIEW, SHORTLISTED, REJECTED

    private boolean autoApplied = false;

    private LocalDateTime appliedAt = LocalDateTime.now();

    public JobApplicationEntity() {
    }

    public JobApplicationEntity(Long jobId, Long userId, String candidateName,
                                String candidateEmail, int matchScore,
                                String matchingSkills, boolean autoApplied) {
        this.jobId = jobId;
        this.userId = userId;
        this.candidateName = candidateName;
        this.candidateEmail = candidateEmail;
        this.matchScore = matchScore;
        this.matchingSkills = matchingSkills;
        this.autoApplied = autoApplied;
        this.status = "APPLIED";
        this.appliedAt = LocalDateTime.now();
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
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
