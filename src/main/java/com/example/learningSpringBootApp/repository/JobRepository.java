package com.example.learningSpringBootApp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.learningSpringBootApp.entity.JobEntity;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {

    List<JobEntity> findByActiveTrueOrderByIdDesc();

    @Query("SELECT j FROM JobEntity j WHERE j.active = true AND (" +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(j.company) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(j.location) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :q, '%'))" +
           ") ORDER BY j.id DESC")
    List<JobEntity> searchJobs(@Param("q") String query);

    @Query("SELECT j FROM JobEntity j WHERE j.active = true AND " +
           "(:type IS NULL OR :type = '' OR LOWER(j.type) = LOWER(:type)) AND " +
           "(:location IS NULL OR :location = '' OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:q IS NULL OR :q = '' OR (" +
           "  LOWER(j.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "  LOWER(j.company) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "  LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "  LOWER(j.description) LIKE LOWER(CONCAT('%', :q, '%'))" +
           ")) ORDER BY j.id DESC")
    List<JobEntity> filterJobs(@Param("q") String query,
                               @Param("type") String type,
                               @Param("location") String location);
}
