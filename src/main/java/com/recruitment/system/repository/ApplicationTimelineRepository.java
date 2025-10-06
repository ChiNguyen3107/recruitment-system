package com.recruitment.system.repository;

import com.recruitment.system.entity.ApplicationTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationTimelineRepository extends JpaRepository<ApplicationTimeline, Long> {

    List<ApplicationTimeline> findByApplicationIdOrderByChangedAtAsc(Long applicationId);
}


