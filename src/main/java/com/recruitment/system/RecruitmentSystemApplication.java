package com.recruitment.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main class cho Recruitment System
 */
@SpringBootApplication
@EnableScheduling
public class RecruitmentSystemApplication {

    public static void main(String[] args) {

        SpringApplication.run(RecruitmentSystemApplication.class, args);
    }
}