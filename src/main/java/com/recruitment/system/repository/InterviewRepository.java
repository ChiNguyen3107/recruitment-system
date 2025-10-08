package com.recruitment.system.repository;

import com.recruitment.system.entity.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByApplicationIdIn(List<Long> applicationIds);

    Page<Interview> findByScheduledAtBetweenOrderByScheduledAtAsc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Interview> findByScheduledByAndScheduledAtBetween(Long scheduledBy, LocalDateTime start, LocalDateTime end);

    List<Interview> findByDbStatusAndScheduledAtBetween(String dbStatus, LocalDateTime start, LocalDateTime end);
}



