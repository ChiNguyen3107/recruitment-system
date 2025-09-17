package com.recruitment.system.repository;

import com.recruitment.system.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Profile entity
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    List<Profile> findByIsPublic(Boolean isPublic);

    Page<Profile> findByIsPublic(Boolean isPublic, Pageable pageable);

    List<Profile> findByCity(String city);

    Page<Profile> findByCity(String city, Pageable pageable);

    @Query("SELECT p FROM Profile p WHERE p.isPublic = true AND " +
           "(LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.skills) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.experience) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Profile> searchPublicProfiles(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Profile p WHERE p.isPublic = true AND " +
           "LOWER(p.skills) LIKE LOWER(CONCAT('%', :skill, '%'))")
    Page<Profile> findBySkill(@Param("skill") String skill, Pageable pageable);

    @Query("SELECT p FROM Profile p WHERE p.isPublic = true AND " +
           "p.desiredSalaryMin <= :maxSalary AND p.desiredSalaryMax >= :minSalary")
    Page<Profile> findBySalaryRange(@Param("minSalary") Long minSalary, 
                                   @Param("maxSalary") Long maxSalary, 
                                   Pageable pageable);

    @Query("SELECT p FROM Profile p WHERE p.isPublic = true AND " +
           "LOWER(p.desiredLocation) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Profile> findByDesiredLocation(@Param("location") String location, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Profile p WHERE p.isPublic = true")
    Long countPublicProfiles();

    @Query("SELECT p FROM Profile p WHERE " +
           "p.summary IS NOT NULL AND p.experience IS NOT NULL AND " +
           "p.education IS NOT NULL AND p.skills IS NOT NULL")
    List<Profile> findCompleteProfiles();

    boolean existsByUserId(Long userId);
}