package com.recruitment.system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interview_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interview_id", nullable = false)
    private Long interviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role", length = 50)
    private String role; // INTERVIEWER | OBSERVER ...
}


