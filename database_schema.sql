CREATE TABLE `users` (
	`id` BIGINT AUTO_INCREMENT,
	`email` VARCHAR(191) NOT NULL UNIQUE,
	`password` VARCHAR(255) NOT NULL,
	`first_name` VARCHAR(75) NOT NULL,
	`last_name` VARCHAR(75) NOT NULL,
	`phone_number` VARCHAR(20),
	`role` ENUM('ADMIN', 'EMPLOYER', 'RECRUITER', 'APPLICANT', 'GUEST') NOT NULL DEFAULT 'APPLICANT',
	`status` ENUM('ACTIVE', 'INACTIVE', 'PENDING', 'SUSPENDED', 'DELETED') NOT NULL DEFAULT 'PENDING',
	`email_verified` BOOLEAN DEFAULT FALSE,
	`verification_token` VARCHAR(255),
	`password_reset_token` VARCHAR(255),
	`password_reset_expires` TIMESTAMP NULL,
	`avatar_url` VARCHAR(500),
	`last_login` TIMESTAMP NULL,
	`company_id` BIGINT,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`),
	INDEX idx_email (`email`),
	INDEX idx_role (`role`),
	INDEX idx_status (`status`)
);


-- Bảng role sẽ được thay thế bằng enum trong bảng users, nên ta bỏ bảng này
-- CREATE TABLE `role` (
-- 	`id` TINYINT AUTO_INCREMENT,
-- 	`code` VARCHAR(50) NOT NULL UNIQUE,
-- 	`name` VARCHAR(100) NOT NULL,
-- 	PRIMARY KEY(`id`)
-- );

-- CREATE TABLE `user_role` (
-- 	`user_id` BIGINT NOT NULL,
-- 	`role_id` TINYINT NOT NULL,
-- 	PRIMARY KEY(`user_id`, `role_id`)
-- );


CREATE TABLE `companies` (
	`id` BIGINT AUTO_INCREMENT,
	`name` VARCHAR(200) NOT NULL,
	`description` TEXT,
	`business_license` VARCHAR(100),
	`tax_code` VARCHAR(30),
	`website` VARCHAR(191),
	`industry` VARCHAR(100),
	`company_size` VARCHAR(50),
	`address` VARCHAR(255),
	`city` VARCHAR(120),
	`country` VARCHAR(120),
	`phone_number` VARCHAR(20),
	`contact_email` VARCHAR(191),
	`logo_url` VARCHAR(500),
	`is_verified` BOOLEAN DEFAULT FALSE,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`),
	INDEX idx_verified (`is_verified`),
	INDEX idx_industry (`industry`),
	INDEX idx_city (`city`)
);


-- Bảng company_member sẽ được thay thế bằng trường company_id trong bảng users
-- CREATE TABLE `company_member` (
-- 	`company_id` BIGINT NOT NULL,
-- 	`user_id` BIGINT NOT NULL,
-- 	`role_in_company` ENUM('OWNER', 'RECRUITER', 'VIEWER') NOT NULL DEFAULT 'RECRUITER',
-- 	`position` VARCHAR(120),
-- 	`department` VARCHAR(120),
-- 	`email` VARCHAR(191),
-- 	`company_phone` CHAR(10),
-- 	`join_date` DATE,
-- 	`status` ENUM('HOAT_DONG', 'TAM_KHOA', 'NGHI'),
-- 	`note` VARCHAR(500),
-- 	PRIMARY KEY(`company_id`, `user_id`)
-- );


CREATE TABLE `profiles` (
	`id` BIGINT AUTO_INCREMENT,
	`user_id` BIGINT NOT NULL UNIQUE,
	`date_of_birth` DATE,
	`gender` ENUM('NAM', 'NU', 'KHAC'),
	`address` VARCHAR(255),
	`city` VARCHAR(120),
	`country` VARCHAR(120),
	`summary` TEXT,
	`experience` TEXT,
	`education` TEXT,
	`skills` TEXT,
	`certifications` TEXT,
	`languages` TEXT,
	`resume_url` VARCHAR(500),
	`linkedin_url` VARCHAR(500),
	`github_url` VARCHAR(500),
	`portfolio_url` VARCHAR(500),
	`desired_salary_min` BIGINT,
	`desired_salary_max` BIGINT,
	`desired_job_type` VARCHAR(50),
	`desired_location` VARCHAR(200),
	`availability` VARCHAR(100),
	`is_public` BOOLEAN DEFAULT FALSE,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`),
	INDEX idx_user (`user_id`),
	INDEX idx_public (`is_public`),
	INDEX idx_city (`city`)
);


-- Bảng attachment có thể giữ nguyên nhưng sẽ tham chiếu đến profiles thay vì candidate
CREATE TABLE `attachments` (
	`id` BIGINT AUTO_INCREMENT,
	`profile_id` BIGINT NOT NULL,
	`type` ENUM('CV', 'COVER_LETTER', 'CHUNG_CHI', 'KHAC') NOT NULL,
	`file_name` VARCHAR(200) NOT NULL,
	`file_extension` VARCHAR(50),
	`file_size` BIGINT,
	`path` VARCHAR(500) NOT NULL,
	`uploaded_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`)
);


-- Bảng skills giữ nguyên
CREATE TABLE `skills` (
	`id` INTEGER AUTO_INCREMENT,
	`name` VARCHAR(120) NOT NULL UNIQUE,
	`description` VARCHAR(255),
	PRIMARY KEY(`id`)
);


-- Bảng profile_skill thay thế candidate_skill
CREATE TABLE `profile_skills` (
	`profile_id` BIGINT NOT NULL,
	`skill_id` INTEGER NOT NULL,
	`level` TINYINT,
	PRIMARY KEY(`profile_id`, `skill_id`)
);


-- Bảng educations giữ nguyên logic nhưng đổi tham chiếu
CREATE TABLE `educations` (
	`id` BIGINT AUTO_INCREMENT,
	`profile_id` BIGINT NOT NULL,
	`school` VARCHAR(200) NOT NULL,
	`major` VARCHAR(200),
	`degree` ENUM('CAO_DANG', 'DAI_HOC', 'THAC_SI', 'TIEN_SI', 'KHAC'),
	`start_year` YEAR,
	`end_year` YEAR,
	`description` TEXT,
	PRIMARY KEY(`id`)
);


-- Bảng work_experiences giữ nguyên logic nhưng đổi tham chiếu  
CREATE TABLE `work_experiences` (
	`id` BIGINT AUTO_INCREMENT,
	`profile_id` BIGINT NOT NULL,
	`company_name` VARCHAR(200) NOT NULL,
	`job_title` VARCHAR(200) NOT NULL,
	`start_date` DATE,
	`end_date` DATE,
	`description` TEXT,
	PRIMARY KEY(`id`)
);


CREATE TABLE `job_postings` (
	`id` BIGINT AUTO_INCREMENT,
	`company_id` BIGINT NOT NULL,
	`created_by` BIGINT NOT NULL,
	`title` VARCHAR(200) NOT NULL,
	`description` LONGTEXT,
	`requirements` LONGTEXT,
	`benefits` LONGTEXT,
	`location` VARCHAR(200),
	`job_type` ENUM('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE') NOT NULL,
	`salary_min` DECIMAL(15,2),
	`salary_max` DECIMAL(15,2),
	`salary_currency` CHAR(3) DEFAULT 'VND',
	`experience_required` VARCHAR(100),
	`education_required` VARCHAR(100),
	`skills_required` TEXT,
	`number_of_positions` INT DEFAULT 1,
	`application_deadline` TIMESTAMP,
	`published_at` TIMESTAMP NULL,
	`views_count` INT DEFAULT 0,
	`applications_count` INT DEFAULT 0,
	`status` ENUM('DRAFT', 'ACTIVE', 'PAUSED', 'CLOSED', 'EXPIRED') NOT NULL DEFAULT 'DRAFT',
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`),
	INDEX idx_status (`status`),
	INDEX idx_company (`company_id`),
	INDEX idx_job_type (`job_type`),
	INDEX idx_location (`location`),
	INDEX idx_deadline (`application_deadline`),
	FULLTEXT idx_search (`title`, `description`, `skills_required`)
);


CREATE TABLE `job_posting_skills` (
	`job_posting_id` BIGINT NOT NULL,
	`skill_id` INTEGER NOT NULL,
	`is_required` TINYINT NOT NULL DEFAULT 1,
	PRIMARY KEY(`job_posting_id`, `skill_id`)
);


CREATE TABLE `applications` (
	`id` BIGINT AUTO_INCREMENT,
	`job_posting_id` BIGINT NOT NULL,
	`applicant_id` BIGINT NOT NULL,
	`status` ENUM('RECEIVED', 'REVIEWED', 'INTERVIEW', 'OFFER', 'HIRED', 'REJECTED') NOT NULL DEFAULT 'RECEIVED',
	`cover_letter` TEXT,
	`resume_url` VARCHAR(500),
	`additional_documents` VARCHAR(1000),
	`interview_date` TIMESTAMP NULL,
	`interview_location` VARCHAR(500),
	`interview_notes` TEXT,
	`feedback` TEXT,
	`rejection_reason` TEXT,
	`offer_details` TEXT,
	`reviewed_at` TIMESTAMP NULL,
	`reviewed_by` BIGINT,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`),
	UNIQUE KEY unique_application (`job_posting_id`, `applicant_id`),
	INDEX idx_status (`status`),
	INDEX idx_job (`job_posting_id`),
	INDEX idx_applicant (`applicant_id`)
);


CREATE TABLE `interview_schedules` (
	`id` BIGINT AUTO_INCREMENT,
	`application_id` BIGINT NOT NULL,
	`company_id` BIGINT NOT NULL,
	`responsible_user_id` BIGINT NOT NULL,
	`start_time` DATETIME NOT NULL,
	`end_time` DATETIME NOT NULL,
	`method` ENUM('ONLINE', 'OFFLINE') NOT NULL,
	`address` VARCHAR(255),
	`note` VARCHAR(500),
	`status` ENUM('MOI_TAO', 'XAC_NHAN', 'HOAN_TAT', 'HUY') NOT NULL DEFAULT 'MOI_TAO',
	PRIMARY KEY(`id`)
);


CREATE TABLE `status_logs` (
	`id` BIGINT AUTO_INCREMENT,
	`application_id` BIGINT NOT NULL,
	`from_status` ENUM('RECEIVED', 'REVIEWED', 'INTERVIEW', 'OFFER', 'HIRED', 'REJECTED'),
	`to_status` ENUM('RECEIVED', 'REVIEWED', 'INTERVIEW', 'OFFER', 'HIRED', 'REJECTED') NOT NULL,
	`executor_id` BIGINT NOT NULL,
	`note` VARCHAR(500),
	`timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`)
);


CREATE TABLE `messages` (
	`id` BIGINT AUTO_INCREMENT,
	`application_id` BIGINT NOT NULL,
	`sender_id` BIGINT NOT NULL,
	`content` TEXT NOT NULL,
	`is_read` TINYINT NOT NULL DEFAULT 0,
	`sent_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`)
);


CREATE TABLE `notifications` (
	`id` BIGINT AUTO_INCREMENT,
	`recipient_id` BIGINT NOT NULL,
	`type` VARCHAR(50) NOT NULL,
	`title` VARCHAR(200) NOT NULL,
	`content` TEXT,
	`is_read` TINYINT NOT NULL DEFAULT 0,
	`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY(`id`)
);


CREATE TABLE `oauth_accounts` (
	`id` BIGINT AUTO_INCREMENT,
	`user_id` BIGINT NOT NULL,
	`provider` ENUM('GOOGLE', 'GITHUB', 'FACEBOOK') NOT NULL,
	`provider_user_id` VARCHAR(191) NOT NULL,
	PRIMARY KEY(`id`)
);


-- Add Foreign Key Constraints

-- Users table
ALTER TABLE `users`
ADD FOREIGN KEY(`company_id`) REFERENCES `companies`(`id`)
ON UPDATE CASCADE ON DELETE SET NULL;

-- Profiles table  
ALTER TABLE `profiles`
ADD FOREIGN KEY(`user_id`) REFERENCES `users`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;

-- Attachments table
ALTER TABLE `attachments`
ADD FOREIGN KEY(`profile_id`) REFERENCES `profiles`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;

-- Profile Skills table
ALTER TABLE `profile_skills`
ADD FOREIGN KEY(`profile_id`) REFERENCES `profiles`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE `profile_skills`
ADD FOREIGN KEY(`skill_id`) REFERENCES `skills`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;

-- Educations table
ALTER TABLE `educations`
ADD FOREIGN KEY(`profile_id`) REFERENCES `profiles`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;

-- Work Experiences table
ALTER TABLE `work_experiences`
ADD FOREIGN KEY(`profile_id`) REFERENCES `profiles`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;

-- Job Postings table
ALTER TABLE `job_postings`
ADD FOREIGN KEY(`company_id`) REFERENCES `companies`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE `job_postings`
ADD FOREIGN KEY(`created_by`) REFERENCES `users`(`id`)
ON UPDATE CASCADE ON DELETE RESTRICT;

-- Job Posting Skills table
ALTER TABLE `job_posting_skills`
ADD FOREIGN KEY(`job_posting_id`) REFERENCES `job_postings`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE `job_posting_skills`
ADD FOREIGN KEY(`skill_id`) REFERENCES `skills`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;

-- Applications table
ALTER TABLE `applications`
ADD FOREIGN KEY(`job_posting_id`) REFERENCES `job_postings`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE `applications`
ADD FOREIGN KEY(`applicant_id`) REFERENCES `users`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;

-- Interview Schedules table
ALTER TABLE `interview_schedules`
ADD FOREIGN KEY(`application_id`) REFERENCES `applications`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE `interview_schedules`
ADD FOREIGN KEY(`company_id`) REFERENCES `companies`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE `interview_schedules`
ADD FOREIGN KEY(`responsible_user_id`) REFERENCES `users`(`id`)
ON UPDATE CASCADE ON DELETE RESTRICT;

-- Status Logs table
ALTER TABLE `status_logs`
ADD FOREIGN KEY(`application_id`) REFERENCES `applications`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE `status_logs`
ADD FOREIGN KEY(`executor_id`) REFERENCES `users`(`id`)
ON UPDATE CASCADE ON DELETE RESTRICT;

-- Messages table
ALTER TABLE `messages`
ADD FOREIGN KEY(`application_id`) REFERENCES `applications`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE `messages`
ADD FOREIGN KEY(`sender_id`) REFERENCES `users`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;

-- Notifications table
ALTER TABLE `notifications`
ADD FOREIGN KEY(`recipient_id`) REFERENCES `users`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;

-- OAuth Accounts table
ALTER TABLE `oauth_accounts`
ADD FOREIGN KEY(`user_id`) REFERENCES `users`(`id`)
ON UPDATE CASCADE ON DELETE CASCADE;

-- Insert sample data for testing

-- Sample companies
INSERT INTO `companies` (`name`, `description`, `industry`, `website`, `city`, `contact_email`, `is_verified`) VALUES
('Tech Innovate Co.', 'Leading technology company specializing in software development', 'Technology', 'https://techinnovate.com', 'Ho Chi Minh City', 'hr@techinnovate.com', TRUE),
('Digital Solutions Ltd.', 'Digital transformation and consulting services', 'Consulting', 'https://digitalsolutions.vn', 'Hanoi', 'jobs@digitalsolutions.vn', TRUE),
('StartUp Hub', 'Innovative startup focusing on mobile applications', 'Technology', 'https://startuphub.vn', 'Da Nang', 'careers@startuphub.vn', FALSE);

-- Sample users
-- Admin user (password: admin123 - $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.)
INSERT INTO `users` (`email`, `password`, `first_name`, `last_name`, `role`, `status`, `email_verified`) VALUES
('admin@recruitment.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin', 'System', 'ADMIN', 'ACTIVE', TRUE);

-- Employer user (password: employer123)
INSERT INTO `users` (`email`, `password`, `first_name`, `last_name`, `role`, `status`, `email_verified`, `company_id`) VALUES
('employer@techinnovate.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'John', 'Manager', 'EMPLOYER', 'ACTIVE', TRUE, 1);

-- Applicant user (password: applicant123)
INSERT INTO `users` (`email`, `password`, `first_name`, `last_name`, `role`, `status`, `email_verified`) VALUES
('applicant@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Jane', 'Developer', 'APPLICANT', 'ACTIVE', TRUE);

-- Sample skills
INSERT INTO `skills` (`name`, `description`) VALUES
('Java', 'Java programming language'),
('Spring Boot', 'Spring Boot framework'),
('React', 'React JavaScript library'),
('MySQL', 'MySQL database'),
('JavaScript', 'JavaScript programming language'),
('Python', 'Python programming language'),
('Docker', 'Container technology'),
('Kubernetes', 'Container orchestration');

-- Sample profile for applicant
INSERT INTO `profiles` (`user_id`, `summary`, `skills`, `experience`, `education`, `is_public`) VALUES
(3, 'Experienced software developer with 3+ years in web development', 'Java, Spring Boot, React, MySQL', '3 years as Full Stack Developer', 'Bachelor in Computer Science', TRUE);

-- Sample job posting
INSERT INTO `job_postings` (`title`, `description`, `requirements`, `job_type`, `status`, `location`, `salary_min`, `salary_max`, 
                         `company_id`, `created_by`, `application_deadline`, `published_at`) VALUES
('Senior Java Developer', 
 'We are looking for an experienced Java developer to join our dynamic team. You will be responsible for developing high-quality applications using Java and Spring Boot.',
 'Bachelor degree in Computer Science, 3+ years Java experience, Spring Boot knowledge, MySQL experience',
 'FULL_TIME', 'ACTIVE', 'Ho Chi Minh City', 20000000, 30000000,
 1, 2, DATE_ADD(NOW(), INTERVAL 30 DAY), NOW());

-- Sample application
INSERT INTO `applications` (`job_posting_id`, `applicant_id`, `cover_letter`) VALUES
(1, 3, 'I am very interested in this position and believe my skills match your requirements.');

-- Link skills to job posting
INSERT INTO `job_posting_skills` (`job_posting_id`, `skill_id`, `is_required`) VALUES
(1, 1, 1), -- Java (required)
(1, 2, 1), -- Spring Boot (required)
(1, 4, 1); -- MySQL (required)

-- Link skills to profile
INSERT INTO `profile_skills` (`profile_id`, `skill_id`, `level`) VALUES
(1, 1, 4), -- Java (level 4)
(1, 2, 4), -- Spring Boot (level 4)
(1, 3, 3), -- React (level 3)
(1, 4, 3); -- MySQL (level 3)