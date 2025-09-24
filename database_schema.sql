-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th9 22, 2025 lúc 08:25 AM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `recruitment_db`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `applications`
--

CREATE TABLE `applications` (
  `id` bigint(20) NOT NULL,
  `job_posting_id` bigint(20) NOT NULL,
  `applicant_id` bigint(20) NOT NULL,
  `status` enum('RECEIVED','REVIEWED','INTERVIEW','OFFER','HIRED','REJECTED') NOT NULL DEFAULT 'RECEIVED',
  `cover_letter` text DEFAULT NULL,
  `resume_url` varchar(500) DEFAULT NULL,
  `additional_documents` varchar(1000) DEFAULT NULL,
  `interview_date` timestamp NULL DEFAULT NULL,
  `interview_location` varchar(500) DEFAULT NULL,
  `interview_notes` text DEFAULT NULL,
  `feedback` text DEFAULT NULL,
  `rejection_reason` text DEFAULT NULL,
  `offer_details` text DEFAULT NULL,
  `reviewed_at` timestamp NULL DEFAULT NULL,
  `reviewed_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `applications`
--

INSERT INTO `applications` (`id`, `job_posting_id`, `applicant_id`, `status`, `cover_letter`, `resume_url`, `additional_documents`, `interview_date`, `interview_location`, `interview_notes`, `feedback`, `rejection_reason`, `offer_details`, `reviewed_at`, `reviewed_by`, `created_at`, `updated_at`) VALUES
(1, 1, 3, 'RECEIVED', 'I am very interested in this position and believe my skills match your requirements.', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-09-16 08:20:18', '2025-09-16 08:20:18');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `attachments`
--

CREATE TABLE `attachments` (
  `id` bigint(20) NOT NULL,
  `profile_id` bigint(20) NOT NULL,
  `type` enum('CV','COVER_LETTER','CHUNG_CHI','KHAC') NOT NULL,
  `file_name` varchar(200) NOT NULL,
  `file_extension` varchar(50) DEFAULT NULL,
  `file_size` bigint(20) DEFAULT NULL,
  `path` varchar(500) NOT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `companies`
--

CREATE TABLE `companies` (
  `id` bigint(20) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `business_license` varchar(100) DEFAULT NULL,
  `tax_code` varchar(30) DEFAULT NULL,
  `website` varchar(191) DEFAULT NULL,
  `industry` varchar(100) DEFAULT NULL,
  `company_size` varchar(50) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `city` varchar(120) DEFAULT NULL,
  `country` varchar(120) DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `contact_email` varchar(191) DEFAULT NULL,
  `logo_url` varchar(500) DEFAULT NULL,
  `is_verified` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `companies`
--

INSERT INTO `companies` (`id`, `name`, `description`, `business_license`, `tax_code`, `website`, `industry`, `company_size`, `address`, `city`, `country`, `phone_number`, `contact_email`, `logo_url`, `is_verified`, `created_at`, `updated_at`) VALUES
(1, 'Tech Innovate Co.', 'Leading technology company specializing in software development', NULL, NULL, 'https://techinnovate.com', 'Technology', NULL, NULL, 'Ho Chi Minh City', NULL, NULL, 'hr@techinnovate.com', NULL, 1, '2025-09-16 08:20:18', '2025-09-16 08:20:18'),
(2, 'Digital Solutions Ltd.', 'Digital transformation and consulting services', NULL, NULL, 'https://digitalsolutions.vn', 'Consulting', NULL, NULL, 'Hanoi', NULL, NULL, 'jobs@digitalsolutions.vn', NULL, 1, '2025-09-16 08:20:18', '2025-09-16 08:20:18'),
(3, 'StartUp Hub', 'Innovative startup focusing on mobile applications', NULL, NULL, 'https://startuphub.vn', 'Technology', NULL, NULL, 'Da Nang', NULL, NULL, 'careers@startuphub.vn', NULL, 0, '2025-09-16 08:20:18', '2025-09-16 08:20:18');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `educations`
--

CREATE TABLE `educations` (
  `id` bigint(20) NOT NULL,
  `profile_id` bigint(20) NOT NULL,
  `school` varchar(200) NOT NULL,
  `major` varchar(200) DEFAULT NULL,
  `degree` enum('CAO_DANG','DAI_HOC','THAC_SI','TIEN_SI','KHAC') DEFAULT NULL,
  `start_year` year(4) DEFAULT NULL,
  `end_year` year(4) DEFAULT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `interview_schedules`
--

CREATE TABLE `interview_schedules` (
  `id` bigint(20) NOT NULL,
  `application_id` bigint(20) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  `responsible_user_id` bigint(20) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `method` enum('ONLINE','OFFLINE') NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `note` varchar(500) DEFAULT NULL,
  `status` enum('MOI_TAO','XAC_NHAN','HOAN_TAT','HUY') NOT NULL DEFAULT 'MOI_TAO'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `job_postings`
--

CREATE TABLE `job_postings` (
  `id` bigint(20) NOT NULL,
  `company_id` bigint(20) NOT NULL,
  `created_by` bigint(20) NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` longtext DEFAULT NULL,
  `requirements` longtext DEFAULT NULL,
  `benefits` longtext DEFAULT NULL,
  `location` varchar(200) DEFAULT NULL,
  `job_type` enum('FULL_TIME','PART_TIME','CONTRACT','INTERNSHIP','FREELANCE') NOT NULL,
  `salary_min` decimal(15,2) DEFAULT NULL,
  `salary_max` decimal(15,2) DEFAULT NULL,
  `salary_currency` char(3) DEFAULT 'VND',
  `experience_required` varchar(100) DEFAULT NULL,
  `education_required` varchar(100) DEFAULT NULL,
  `skills_required` text DEFAULT NULL,
  `number_of_positions` int(11) DEFAULT 1,
  `application_deadline` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `published_at` timestamp NULL DEFAULT NULL,
  `views_count` int(11) DEFAULT 0,
  `applications_count` int(11) DEFAULT 0,
  `status` enum('DRAFT','ACTIVE','PAUSED','CLOSED','EXPIRED') NOT NULL DEFAULT 'DRAFT',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `job_postings`
--

INSERT INTO `job_postings` (`id`, `company_id`, `created_by`, `title`, `description`, `requirements`, `benefits`, `location`, `job_type`, `salary_min`, `salary_max`, `salary_currency`, `experience_required`, `education_required`, `skills_required`, `number_of_positions`, `application_deadline`, `published_at`, `views_count`, `applications_count`, `status`, `created_at`, `updated_at`) VALUES
(1, 1, 2, 'Senior Java Developer (Updated)', 'Updated job description for Java developer.', 'Bachelor degree in Computer Science, 5+ years Java experience, Spring Boot knowledge, MySQL experience', 'Attractive salary, Flexible working hours, Health insurance, Bonus', 'Ho Chi Minh City', 'FULL_TIME', 25000000.00, 35000000.00, 'VND', NULL, NULL, NULL, 2, '2025-12-31 16:59:59', '2025-09-16 08:20:18', 0, 0, 'ACTIVE', '2025-09-16 08:20:18', '2025-09-17 07:02:11'),
(3, 1, 2, 'Senior Java Developer', 'We are looking for an experienced Java developer to join our dynamic team. You will be responsible for developing high-quality applications using Java and Spring Boot.', 'Bachelor degree in Computer Science, 3+ years Java experience, Spring Boot knowledge, MySQL experience', 'Attractive salary, Flexible working hours, Health insurance', 'Ho Chi Minh City', 'FULL_TIME', 20000000.00, 30000000.00, 'VND', NULL, NULL, NULL, 1, '2025-10-16 08:20:18', NULL, 0, 0, 'ACTIVE', '2025-09-17 07:16:25', '2025-09-17 07:16:25'),
(4, 1, 2, 'Lập trình viên Java cấp cao', 'Chúng tôi cần tuyển Lập trình viên Java có kinh nghiệm tham gia phát triển các ứng dụng doanh nghiệp với Java và Spring Boot.', 'Tốt nghiệp Đại học ngành CNTT, 3+ năm kinh nghiệm Java, thành thạo Spring Boot, có kinh nghiệm MySQL', 'Lương hấp dẫn, Giờ làm việc linh hoạt, Bảo hiểm sức khỏe', 'TP. Hồ Chí Minh', 'FULL_TIME', 20000000.00, 30000000.00, 'VND', NULL, NULL, NULL, 1, '2025-10-16 08:20:18', NULL, 0, 0, 'ACTIVE', '2025-09-17 07:18:05', '2025-09-17 07:18:05');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `job_posting_skills`
--

CREATE TABLE `job_posting_skills` (
  `job_posting_id` bigint(20) NOT NULL,
  `skill_id` int(11) NOT NULL,
  `is_required` tinyint(4) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `job_posting_skills`
--

INSERT INTO `job_posting_skills` (`job_posting_id`, `skill_id`, `is_required`) VALUES
(1, 1, 1),
(1, 2, 1),
(1, 4, 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `messages`
--

CREATE TABLE `messages` (
  `id` bigint(20) NOT NULL,
  `application_id` bigint(20) NOT NULL,
  `sender_id` bigint(20) NOT NULL,
  `content` text NOT NULL,
  `is_read` tinyint(4) NOT NULL DEFAULT 0,
  `sent_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `notifications`
--

CREATE TABLE `notifications` (
  `id` bigint(20) NOT NULL,
  `recipient_id` bigint(20) NOT NULL,
  `type` varchar(50) NOT NULL,
  `title` varchar(200) NOT NULL,
  `content` text DEFAULT NULL,
  `is_read` tinyint(4) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `oauth_accounts`
--

CREATE TABLE `oauth_accounts` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `provider` enum('GOOGLE','GITHUB','FACEBOOK') NOT NULL,
  `provider_user_id` varchar(191) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `profiles`
--

CREATE TABLE `profiles` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `gender` enum('NAM','NU','KHAC') DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `city` varchar(120) DEFAULT NULL,
  `country` varchar(120) DEFAULT NULL,
  `summary` text DEFAULT NULL,
  `experience` text DEFAULT NULL,
  `education` text DEFAULT NULL,
  `skills` text DEFAULT NULL,
  `certifications` text DEFAULT NULL,
  `languages` text DEFAULT NULL,
  `resume_url` varchar(500) DEFAULT NULL,
  `linkedin_url` varchar(500) DEFAULT NULL,
  `github_url` varchar(500) DEFAULT NULL,
  `portfolio_url` varchar(500) DEFAULT NULL,
  `desired_salary_min` bigint(20) DEFAULT NULL,
  `desired_salary_max` bigint(20) DEFAULT NULL,
  `desired_job_type` varchar(50) DEFAULT NULL,
  `desired_location` varchar(200) DEFAULT NULL,
  `availability` varchar(100) DEFAULT NULL,
  `is_public` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `profiles`
--

INSERT INTO `profiles` (`id`, `user_id`, `date_of_birth`, `gender`, `address`, `city`, `country`, `summary`, `experience`, `education`, `skills`, `certifications`, `languages`, `resume_url`, `linkedin_url`, `github_url`, `portfolio_url`, `desired_salary_min`, `desired_salary_max`, `desired_job_type`, `desired_location`, `availability`, `is_public`, `created_at`, `updated_at`) VALUES
(1, 3, NULL, NULL, NULL, NULL, NULL, 'Experienced software developer with 3+ years in web development', '3 years as Full Stack Developer', 'Bachelor in Computer Science', 'Java, Spring Boot, React, MySQL', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2025-09-16 08:20:18', '2025-09-16 08:20:18'),
(3, 5, '1990-01-15', NULL, '123 Đường NVL, Quận 1, TP.HCM', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, '2025-09-16 09:16:08', '2025-09-17 06:38:51');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `profile_skills`
--

CREATE TABLE `profile_skills` (
  `profile_id` bigint(20) NOT NULL,
  `skill_id` int(11) NOT NULL,
  `level` tinyint(4) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `profile_skills`
--

INSERT INTO `profile_skills` (`profile_id`, `skill_id`, `level`) VALUES
(1, 1, 4),
(1, 2, 4),
(1, 3, 3),
(1, 4, 3);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `skills`
--

CREATE TABLE `skills` (
  `id` int(11) NOT NULL,
  `name` varchar(120) NOT NULL,
  `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `skills`
--

INSERT INTO `skills` (`id`, `name`, `description`) VALUES
(1, 'Java', 'Java programming language'),
(2, 'Spring Boot', 'Spring Boot framework'),
(3, 'React', 'React JavaScript library'),
(4, 'MySQL', 'MySQL database'),
(5, 'JavaScript', 'JavaScript programming language'),
(6, 'Python', 'Python programming language'),
(7, 'Docker', 'Container technology'),
(8, 'Kubernetes', 'Container orchestration');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `status_logs`
--

CREATE TABLE `status_logs` (
  `id` bigint(20) NOT NULL,
  `application_id` bigint(20) NOT NULL,
  `from_status` enum('RECEIVED','REVIEWED','INTERVIEW','OFFER','HIRED','REJECTED') DEFAULT NULL,
  `to_status` enum('RECEIVED','REVIEWED','INTERVIEW','OFFER','HIRED','REJECTED') NOT NULL,
  `executor_id` bigint(20) NOT NULL,
  `note` varchar(500) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `email` varchar(191) NOT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(75) NOT NULL,
  `last_name` varchar(75) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `role` enum('ADMIN','EMPLOYER','RECRUITER','APPLICANT','GUEST') NOT NULL DEFAULT 'APPLICANT',
  `status` enum('ACTIVE','INACTIVE','PENDING','SUSPENDED','DELETED') NOT NULL DEFAULT 'PENDING',
  `email_verified` tinyint(1) DEFAULT 0,
  `verification_token` varchar(255) DEFAULT NULL,
  `password_reset_token` varchar(255) DEFAULT NULL,
  `password_reset_expires` timestamp NULL DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `last_login` timestamp NULL DEFAULT NULL,
  `company_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`id`, `email`, `password`, `first_name`, `last_name`, `phone_number`, `role`, `status`, `email_verified`, `verification_token`, `password_reset_token`, `password_reset_expires`, `avatar_url`, `last_login`, `company_id`, `created_at`, `updated_at`) VALUES
(1, 'admin@recruitment.com', '$2a$10$LdjS4heeg.BavaGecRZj6O8YXRK7M5aDbld3uboBRYZsWe4Kb1k3a', 'Admin', 'System', NULL, 'ADMIN', 'ACTIVE', 1, NULL, NULL, NULL, NULL, NULL, NULL, '2025-09-16 08:20:18', '2025-09-19 05:56:42'),
(2, 'employer@techinnovate.com', '$2a$10$LdjS4heeg.BavaGecRZj6O8YXRK7M5aDbld3uboBRYZsWe4Kb1k3a', 'John', 'Manager', NULL, 'EMPLOYER', 'ACTIVE', 1, NULL, NULL, NULL, NULL, NULL, 1, '2025-09-16 08:20:18', '2025-09-17 04:41:56'),
(3, 'applicant@test.com', '$2a$10$LdjS4heeg.BavaGecRZj6O8YXRK7M5aDbld3uboBRYZsWe4Kb1k3a', 'Jane', 'Developer', NULL, 'APPLICANT', 'ACTIVE', 1, NULL, NULL, NULL, NULL, NULL, NULL, '2025-09-16 08:20:18', '2025-09-17 04:41:52'),
(5, 'john.doe@example.com', '$2a$10$LdjS4heeg.BavaGecRZj6O8YXRK7M5aDbld3uboBRYZsWe4Kb1k3a', 'John', 'Doe', NULL, 'APPLICANT', 'ACTIVE', 1, '3a9f742d-1c78-4dfa-963b-ce257875b043', NULL, NULL, NULL, NULL, NULL, '2025-09-16 09:16:08', '2025-09-17 02:36:09');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `work_experiences`
--

CREATE TABLE `work_experiences` (
  `id` bigint(20) NOT NULL,
  `profile_id` bigint(20) NOT NULL,
  `company_name` varchar(200) NOT NULL,
  `job_title` varchar(200) NOT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `applications`
--
ALTER TABLE `applications`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_application` (`job_posting_id`,`applicant_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_job` (`job_posting_id`),
  ADD KEY `idx_applicant` (`applicant_id`);

--
-- Chỉ mục cho bảng `attachments`
--
ALTER TABLE `attachments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `profile_id` (`profile_id`);

--
-- Chỉ mục cho bảng `companies`
--
ALTER TABLE `companies`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_verified` (`is_verified`),
  ADD KEY `idx_industry` (`industry`),
  ADD KEY `idx_city` (`city`);

--
-- Chỉ mục cho bảng `educations`
--
ALTER TABLE `educations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `profile_id` (`profile_id`);

--
-- Chỉ mục cho bảng `interview_schedules`
--
ALTER TABLE `interview_schedules`
  ADD PRIMARY KEY (`id`),
  ADD KEY `application_id` (`application_id`),
  ADD KEY `company_id` (`company_id`),
  ADD KEY `responsible_user_id` (`responsible_user_id`);

--
-- Chỉ mục cho bảng `job_postings`
--
ALTER TABLE `job_postings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_company` (`company_id`),
  ADD KEY `idx_job_type` (`job_type`),
  ADD KEY `idx_location` (`location`),
  ADD KEY `idx_deadline` (`application_deadline`),
  ADD KEY `created_by` (`created_by`);
ALTER TABLE `job_postings` ADD FULLTEXT KEY `idx_search` (`title`,`description`,`skills_required`);

--
-- Chỉ mục cho bảng `job_posting_skills`
--
ALTER TABLE `job_posting_skills`
  ADD PRIMARY KEY (`job_posting_id`,`skill_id`),
  ADD KEY `skill_id` (`skill_id`);

--
-- Chỉ mục cho bảng `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `application_id` (`application_id`),
  ADD KEY `sender_id` (`sender_id`);

--
-- Chỉ mục cho bảng `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `recipient_id` (`recipient_id`);

--
-- Chỉ mục cho bảng `oauth_accounts`
--
ALTER TABLE `oauth_accounts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Chỉ mục cho bảng `profiles`
--
ALTER TABLE `profiles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD KEY `idx_user` (`user_id`),
  ADD KEY `idx_public` (`is_public`),
  ADD KEY `idx_city` (`city`);

--
-- Chỉ mục cho bảng `profile_skills`
--
ALTER TABLE `profile_skills`
  ADD PRIMARY KEY (`profile_id`,`skill_id`),
  ADD KEY `skill_id` (`skill_id`);

--
-- Chỉ mục cho bảng `skills`
--
ALTER TABLE `skills`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Chỉ mục cho bảng `status_logs`
--
ALTER TABLE `status_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `application_id` (`application_id`),
  ADD KEY `executor_id` (`executor_id`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `idx_role` (`role`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `company_id` (`company_id`);

--
-- Chỉ mục cho bảng `work_experiences`
--
ALTER TABLE `work_experiences`
  ADD PRIMARY KEY (`id`),
  ADD KEY `profile_id` (`profile_id`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `applications`
--
ALTER TABLE `applications`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT cho bảng `attachments`
--
ALTER TABLE `attachments`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `companies`
--
ALTER TABLE `companies`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT cho bảng `educations`
--
ALTER TABLE `educations`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `interview_schedules`
--
ALTER TABLE `interview_schedules`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `job_postings`
--
ALTER TABLE `job_postings`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT cho bảng `messages`
--
ALTER TABLE `messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `oauth_accounts`
--
ALTER TABLE `oauth_accounts`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `profiles`
--
ALTER TABLE `profiles`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT cho bảng `skills`
--
ALTER TABLE `skills`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT cho bảng `status_logs`
--
ALTER TABLE `status_logs`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT cho bảng `work_experiences`
--
ALTER TABLE `work_experiences`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `applications`
--
ALTER TABLE `applications`
  ADD CONSTRAINT `applications_ibfk_1` FOREIGN KEY (`job_posting_id`) REFERENCES `job_postings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `applications_ibfk_2` FOREIGN KEY (`applicant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `attachments`
--
ALTER TABLE `attachments`
  ADD CONSTRAINT `attachments_ibfk_1` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `educations`
--
ALTER TABLE `educations`
  ADD CONSTRAINT `educations_ibfk_1` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `interview_schedules`
--
ALTER TABLE `interview_schedules`
  ADD CONSTRAINT `interview_schedules_ibfk_1` FOREIGN KEY (`application_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `interview_schedules_ibfk_2` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `interview_schedules_ibfk_3` FOREIGN KEY (`responsible_user_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `job_postings`
--
ALTER TABLE `job_postings`
  ADD CONSTRAINT `job_postings_ibfk_1` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `job_postings_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `job_posting_skills`
--
ALTER TABLE `job_posting_skills`
  ADD CONSTRAINT `job_posting_skills_ibfk_1` FOREIGN KEY (`job_posting_id`) REFERENCES `job_postings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `job_posting_skills_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`application_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `oauth_accounts`
--
ALTER TABLE `oauth_accounts`
  ADD CONSTRAINT `oauth_accounts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `profiles`
--
ALTER TABLE `profiles`
  ADD CONSTRAINT `profiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `profile_skills`
--
ALTER TABLE `profile_skills`
  ADD CONSTRAINT `profile_skills_ibfk_1` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `profile_skills_ibfk_2` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `status_logs`
--
ALTER TABLE `status_logs`
  ADD CONSTRAINT `status_logs_ibfk_1` FOREIGN KEY (`application_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `status_logs_ibfk_2` FOREIGN KEY (`executor_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Các ràng buộc cho bảng `work_experiences`
--
ALTER TABLE `work_experiences`
  ADD CONSTRAINT `work_experiences_ibfk_1` FOREIGN KEY (`profile_id`) REFERENCES `profiles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
