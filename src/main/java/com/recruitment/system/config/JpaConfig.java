package com.recruitment.system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Cấu hình JPA
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.recruitment.system.repository")
@EnableJpaAuditing
public class JpaConfig {
}