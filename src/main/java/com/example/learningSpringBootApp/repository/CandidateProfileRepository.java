package com.example.learningSpringBootApp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.learningSpringBootApp.entity.CandidateProfileEntity;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfileEntity, Long> {

    Optional<CandidateProfileEntity> findByUserId(Long userId);

    Optional<CandidateProfileEntity> findByEmail(String email);
}
