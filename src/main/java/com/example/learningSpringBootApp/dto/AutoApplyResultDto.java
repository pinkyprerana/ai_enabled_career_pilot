package com.example.learningSpringBootApp.dto;

import java.util.ArrayList;
import java.util.List;

public class AutoApplyResultDto {

    private int evaluatedJobs;
    private int appliedCount;
    private int alreadyAppliedCount;
    private int skippedLowMatchCount;
    private List<JobApplicationDto> newlyAppliedJobs = new ArrayList<>();
    private String message;

    public AutoApplyResultDto() {
    }

    public AutoApplyResultDto(int evaluatedJobs, int appliedCount, int alreadyAppliedCount,
                              int skippedLowMatchCount, List<JobApplicationDto> newlyAppliedJobs,
                              String message) {
        this.evaluatedJobs = evaluatedJobs;
        this.appliedCount = appliedCount;
        this.alreadyAppliedCount = alreadyAppliedCount;
        this.skippedLowMatchCount = skippedLowMatchCount;
        this.newlyAppliedJobs = newlyAppliedJobs != null ? newlyAppliedJobs : new ArrayList<>();
        this.message = message;
    }

    public int getEvaluatedJobs() {
        return evaluatedJobs;
    }

    public void setEvaluatedJobs(int evaluatedJobs) {
        this.evaluatedJobs = evaluatedJobs;
    }

    public int getAppliedCount() {
        return appliedCount;
    }

    public void setAppliedCount(int appliedCount) {
        this.appliedCount = appliedCount;
    }

    public int getAlreadyAppliedCount() {
        return alreadyAppliedCount;
    }

    public void setAlreadyAppliedCount(int alreadyAppliedCount) {
        this.alreadyAppliedCount = alreadyAppliedCount;
    }

    public int getSkippedLowMatchCount() {
        return skippedLowMatchCount;
    }

    public void setSkippedLowMatchCount(int skippedLowMatchCount) {
        this.skippedLowMatchCount = skippedLowMatchCount;
    }

    public List<JobApplicationDto> getNewlyAppliedJobs() {
        return newlyAppliedJobs;
    }

    public void setNewlyAppliedJobs(List<JobApplicationDto> newlyAppliedJobs) {
        this.newlyAppliedJobs = newlyAppliedJobs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
