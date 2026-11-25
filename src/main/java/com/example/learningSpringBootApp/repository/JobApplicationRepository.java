package com.example.learningSpringBootApp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.learningSpringBootApp.entity.JobApplicationEntity;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplicationEntity, Long> {

    List<JobApplicationEntity> findByUserIdOrderByAppliedAtDesc(Long userId);

    boolean existsByJobIdAndUserId(Long jobId, Long userId);

    Optional<JobApplicationEntity> findByJobIdAndUserId(Long jobId, Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndAutoAppliedTrue(Long userId);
}
