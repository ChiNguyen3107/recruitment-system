package com.recruitment.system.repository;

import com.recruitment.system.entity.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    boolean existsByUserIdAndJobPostingId(Long userId, Long jobPostingId);

    Optional<SavedJob> findByUserIdAndJobPostingId(Long userId, Long jobPostingId);

    Page<SavedJob> findByUserIdOrderBySavedAtDesc(Long userId, Pageable pageable);

    void deleteByUserIdAndJobPostingId(Long userId, Long jobPostingId);

    long countByUserId(Long userId);

    @Query("select sj.jobPostingId from SavedJob sj " +
            "where sj.userId = :userId and sj.jobPostingId in :jobIds")
    List<Long> findSavedJobIdsIn(@Param("userId") Long userId,
                                 @Param("jobIds") Collection<Long> jobIds);

}