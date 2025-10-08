package com.recruitment.system.repository;

import com.recruitment.system.entity.InterviewParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewParticipantRepository extends JpaRepository<InterviewParticipant, Long> {
    List<InterviewParticipant> findByInterviewId(Long interviewId);
    boolean existsByInterviewIdAndUserId(Long interviewId, Long userId);
    void deleteByInterviewIdAndUserId(Long interviewId, Long userId);
}


