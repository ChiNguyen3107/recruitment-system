-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th10 06, 2025 lúc 10:25 AM
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
(2, 4, 5, 'REVIEWED', 'Thư xin việc mẫu...', 'https://example.com/cv.pdf', NULL, NULL, NULL, NULL, 'Đã xem hồ sơ', NULL, NULL, '2025-09-29 07:53:47', 20, '2025-09-25 09:36:51', '2025-09-29 07:54:15');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `application_timelines`
--

CREATE TABLE `application_timelines` (
  `id` bigint(20) NOT NULL,
  `application_id` bigint(20) NOT NULL,
  `from_status` enum('RECEIVED','REVIEWED','INTERVIEW','OFFER','HIRED','REJECTED','WITHDRAWN') DEFAULT NULL,
  `to_status` enum('RECEIVED','REVIEWED','INTERVIEW','OFFER','HIRED','REJECTED','WITHDRAWN') NOT NULL,
  `note` text DEFAULT NULL,
  `changed_by` bigint(20) DEFAULT NULL,
  `changed_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `benefits` text DEFAULT NULL,
  `working_hours` varchar(100) DEFAULT NULL,
  `company_photos` text DEFAULT NULL,
  `social_links` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `companies`
--

INSERT INTO `companies` (`id`, `name`, `description`, `business_license`, `tax_code`, `website`, `industry`, `company_size`, `address`, `city`, `country`, `phone_number`, `contact_email`, `logo_url`, `is_verified`, `created_at`, `updated_at`, `benefits`, `working_hours`, `company_photos`, `social_links`) VALUES
(1, 'Tech Innovate Co.', 'Leading technology company specializing in software development', NULL, NULL, 'https://techinnovate.com', 'Technology', NULL, NULL, 'Ho Chi Minh City', NULL, NULL, 'hr@techinnovate.com', NULL, 1, '2025-09-16 08:20:18', '2025-09-16 08:20:18', NULL, NULL, NULL, NULL),
(2, 'Digital Solutions Ltd.', 'Digital transformation and consulting services', NULL, NULL, 'https://digitalsolutions.vn', 'Consulting', NULL, NULL, 'Hanoi', NULL, NULL, 'jobs@digitalsolutions.vn', NULL, 1, '2025-09-16 08:20:18', '2025-09-16 08:20:18', NULL, NULL, NULL, NULL),
(3, 'StartUp Hub', 'Innovative startup focusing on mobile applications', NULL, NULL, 'https://startuphub.vn', 'Technology', NULL, NULL, 'Da Nang', NULL, NULL, 'careers@startuphub.vn', NULL, 0, '2025-09-16 08:20:18', '2025-09-16 08:20:18', NULL, NULL, NULL, NULL),
(4, 'Tech Company Ltd', 'Leading technology company', NULL, NULL, 'https://techcompany.com', 'Technology', NULL, '123 Tech Street, Ho Chi Minh City', NULL, NULL, NULL, NULL, NULL, 0, '2025-09-22 07:38:10', '2025-09-22 07:38:10', NULL, NULL, NULL, NULL),
(5, 'Tech Innovate Co.', 'Mô tả công ty cập nhật', NULL, NULL, 'https://techinnovate.com', 'Technology', 'MEDIUM', '123 Tech Street', 'Ho Chi Minh City', 'Vietnam', '0900000000', 'hr@techinnovate.com', NULL, 0, '2025-09-22 07:41:38', '2025-10-06 08:04:59', '[\"Health Insurance\",\"Remote Work\",\"13th Month Salary\"]', '9:00-18:00', '[\"https://example.com/photo1.jpg\",\"https://example.com/photo2.jpg\"]', '{\"facebook\":\"https://facebook.com/techinnovate\",\"linkedin\":\"https://linkedin.com/company/techinnovate\",\"twitter\":\"https://twitter.com/techinnovate\"}');

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
  `application_deadline` datetime NOT NULL,
  `published_at` datetime DEFAULT NULL,
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
(1, 1, 2, 'Senior Java Developer (Updated)', 'Updated job description for Java developer.', 'Bachelor degree in Computer Science, 5+ years Java experience, Spring Boot knowledge, MySQL experience', 'Attractive salary, Flexible working hours, Health insurance, Bonus', 'Ho Chi Minh City', 'FULL_TIME', 25000000.00, 35000000.00, 'VND', NULL, NULL, NULL, 2, '2025-12-31 23:59:59', '2025-09-16 15:20:18', 0, 0, 'ACTIVE', '2025-09-16 08:20:18', '2025-09-17 07:02:11'),
(3, 1, 2, 'Senior Java Developer', 'We are looking for an experienced Java developer to join our dynamic team. You will be responsible for developing high-quality applications using Java and Spring Boot.', 'Bachelor degree in Computer Science, 3+ years Java experience, Spring Boot knowledge, MySQL experience', 'Attractive salary, Flexible working hours, Health insurance', 'Ho Chi Minh City', 'FULL_TIME', 20000000.00, 30000000.00, 'VND', NULL, NULL, NULL, 1, '2025-10-16 15:20:18', NULL, 0, 0, 'ACTIVE', '2025-09-17 07:16:25', '2025-09-17 07:16:25'),
(4, 4, 2, 'Lập trình viên Java cấp cao', 'Chúng tôi cần tuyển Lập trình viên Java có kinh nghiệm tham gia phát triển các ứng dụng doanh nghiệp với Java và Spring Boot.', 'Tốt nghiệp Đại học ngành CNTT, 3+ năm kinh nghiệm Java, thành thạo Spring Boot, có kinh nghiệm MySQL', 'Lương hấp dẫn, Giờ làm việc linh hoạt, Bảo hiểm sức khỏe', 'TP. Hồ Chí Minh', 'FULL_TIME', 20000000.00, 30000000.00, 'VND', NULL, NULL, NULL, 1, '2025-10-16 15:20:18', NULL, 0, 1, 'ACTIVE', '2025-09-17 07:18:05', '2025-10-02 07:31:57');

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
(3, 5, '1995-05-15', 'NAM', '123 Đường ABC', 'Hà Nội', 'Việt Nam', 'Lập trình viên Java với 3 năm kinh nghiệm phát triển web application. Có kinh nghiệm với Spring Boot, MySQL, và các công nghệ frontend.', '• Công ty ABC (2021-2024): Lập trình viên Java\n  - Phát triển RESTful API với Spring Boot\n  - Thiết kế và quản lý database MySQL\n  - Làm việc với team 5 người\n  - Đóng góp vào việc tối ưu hóa hiệu suất hệ thống\n\n• Dự án cá nhân (2020-2021)\n  - Xây dựng ứng dụng quản lý bán hàng\n  - Sử dụng Java, Spring MVC, JSP', '• Đại học Bách Khoa Hà Nội (2017-2021)\n  - Chuyên ngành: Công nghệ thông tin\n  - GPA: 3.2/4.0\n  - Đồ án tốt nghiệp: Hệ thống quản lý thư viện', '• Lập trình: Java, Spring Boot, Spring Security, JPA/Hibernate\n• Database: MySQL, PostgreSQL\n• Frontend: HTML, CSS, JavaScript, React\n• Tools: Git, Maven, IntelliJ IDEA, Docker\n• Ngôn ngữ: Tiếng Anh (TOEIC 750)', '• Oracle Certified Professional Java SE 8 Programmer (2022)\n• AWS Certified Developer Associate (2023)\n• Scrum Master Certification (2023)', '• Tiếng Việt: Bản ngữ\n• Tiếng Anh: Trung bình khá (TOEIC 750)\n• Tiếng Nhật: Cơ bản (N4)', '/uploads/resumes/5/resume-1758790857394.pdf', 'https://linkedin.com/in/nguyenvana', 'https://github.com/nguyenvana', 'https://nguyenvana.dev', 15000000, 25000000, 'FULL_TIME', 'Hà Nội', 'Có thể bắt đầu ngay', 1, '2025-09-16 09:16:08', '2025-09-25 09:00:57'),
(14, 22, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, '2025-09-29 06:56:05', '2025-09-29 06:56:05');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `profile_skills`
--

CREATE TABLE `profile_skills` (
  `profile_id` bigint(20) NOT NULL,
  `skill_id` int(11) NOT NULL,
  `level` tinyint(4) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `refresh_tokens`
--

CREATE TABLE `refresh_tokens` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `token` varchar(512) NOT NULL,
  `expires_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `revoked` tinyint(1) NOT NULL DEFAULT 0,
  `replaced_by_token` varchar(512) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `refresh_tokens`
--

INSERT INTO `refresh_tokens` (`id`, `user_id`, `token`, `expires_at`, `revoked`, `replaced_by_token`, `created_at`, `updated_at`) VALUES
(8, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHNkdHVkZW50LmN0dS5lZHUudm4iLCJpYXQiOjE3NTg3MDAzMTYsImV4cCI6MTc2MTI5MjMxNn0.xM1SgpkH90kvsX3aNAlURurJQPXVUYc1hum7LApiPSreMqC-5FZDGMk7LGpUwiAm', '2025-10-24 07:51:56', 1, NULL, '2025-09-24 07:51:56', '2025-09-24 07:55:31'),
(9, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1ODcwMDUzMSwiZXhwIjoxNzYxMjkyNTMxfQ.-slWou_RkO-s9N4OtofHYO4AbFBmiAnmHk6vvh4BZwFI6PphQls1SX5kqcptfJ22', '2025-10-24 07:55:31', 1, NULL, '2025-09-24 07:55:31', '2025-09-24 08:25:07'),
(10, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1ODcwMjMwNywiZXhwIjoxNzYxMjk0MzA3fQ.b9L2PxinMv9wuuALrFaSqkLEoy4kgipZLXo-jfhHn1jHA3N5dB_U5Ej2v3cF5WOh', '2025-10-24 08:25:07', 1, NULL, '2025-09-24 08:25:07', '2025-09-24 08:49:36'),
(15, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1ODcwMzc3NiwiZXhwIjoxNzYxMjk1Nzc2fQ.10bRNSoUkxFT9XQPByZpRMLwd4pbHkl4myXjTWJdURnHCbnXGYU_5tggqrCD4xk2', '2025-10-24 08:49:36', 1, NULL, '2025-09-24 08:49:36', '2025-09-24 08:53:34'),
(16, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1ODcwNDAxNywiZXhwIjoxNzYxMjk2MDE3fQ.tUfuNNG3mbfOv3uGVn2_ufTMR0Dxjpj1Tc-7EjmkXaJ3aYaEyPWFdeTO-vSRiah4', '2025-10-24 08:53:37', 1, NULL, '2025-09-24 08:53:37', '2025-09-24 08:56:46'),
(17, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1ODcwNDIwNiwiZXhwIjoxNzYxMjk2MjA2fQ.UWHXlYewrpnAlWsYPedZULCNJXYBIv9Gesf5-tD-Y4t4BqGP0qaLq5deOVXWcbbY', '2025-10-24 08:56:46', 1, NULL, '2025-09-24 08:56:46', '2025-09-29 07:33:04'),
(18, 5, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzU4Nzg0MjU3LCJleHAiOjE3NjEzNzYyNTd9.FifuTyZ_Z9X_zzNvvNBonAl4ViGQwFOFGdbcsZoEAmPmCiuHkZGIzoXEm6TqeRPR', '2025-10-25 07:10:57', 1, NULL, '2025-09-25 07:10:57', '2025-09-25 07:22:29'),
(19, 5, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzU4Nzg0OTQ5LCJleHAiOjE3NjEzNzY5NDl9.yF3-j1ha10He5XPzL2mtsZoEQlariBhZCYZSdmH4bck5_DsYnOUCKUkf6VkP-XYu', '2025-10-25 07:22:29', 1, NULL, '2025-09-25 07:22:29', '2025-09-25 07:26:57'),
(20, 1, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImFkbWluQHJlY3J1aXRtZW50LmNvbSIsImlhdCI6MTc1ODc4NTI0NSwiZXhwIjoxNzYxMzc3MjQ1fQ.bekrFrOiHpP6sQLKXf16njhGyGRF4nvbcELqqTgh_T4nssADlNmE7iMW-ri12hWv', '2025-10-25 07:27:25', 1, NULL, '2025-09-25 07:27:25', '2025-09-25 07:32:02'),
(21, 1, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImFkbWluQHJlY3J1aXRtZW50LmNvbSIsImlhdCI6MTc1ODc4NTUyMiwiZXhwIjoxNzYxMzc3NTIyfQ.4FigP97XNNkk9loCviEITZM60C6XM-AU1R3m0zeKt-5imqxJ-y2cZorYPQL1lRWA', '2025-10-25 07:32:02', 1, NULL, '2025-09-25 07:32:02', '2025-09-25 07:42:32'),
(22, 21, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6InJlY3J1aXRlckBleGFtcGxlLmNvbSIsImlhdCI6MTc1ODc4NTc4NCwiZXhwIjoxNzYxMzc3Nzg0fQ._ht9_B-a--o9gmRcf_iP6444vBSXofOwt4fNbUfnBkmxmXoSplqXb0bn4LQBYfrn', '2025-10-25 07:36:24', 1, NULL, '2025-09-25 07:36:24', '2025-09-25 07:42:11'),
(24, 21, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6InJlY3J1aXRlckBleGFtcGxlLmNvbSIsImlhdCI6MTc1ODc4NjEzMSwiZXhwIjoxNzYxMzc4MTMxfQ.DmsDhEiRONvB_az9K8tvWrso-zveGGhqrWVSCn9gOH1efIS_OQmcNValzX8YzxnL', '2025-10-25 07:42:11', 1, NULL, '2025-09-25 07:42:11', '2025-09-25 07:46:57'),
(25, 1, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImFkbWluQHJlY3J1aXRtZW50LmNvbSIsImlhdCI6MTc1ODc4NjE1MiwiZXhwIjoxNzYxMzc4MTUyfQ.SocZNWUU4iXbAABIH6bw5xLNDG2rVN6TFXFrcrmAxCKtcvTxfyBVpxlnjZpacFNn', '2025-10-25 07:42:32', 1, NULL, '2025-09-25 07:42:32', '2025-09-25 07:53:48'),
(26, 5, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzU4Nzg2MzI2LCJleHAiOjE3NjEzNzgzMjZ9.rOe9lQEipBYtQpjYKwPuo8pFVU3M3LqiTZ7L4i_x7UERkdkPBehiLRVL2JhUt1SO', '2025-10-25 07:45:26', 1, NULL, '2025-09-25 07:45:26', '2025-09-25 07:54:19'),
(27, 21, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6InJlY3J1aXRlckBleGFtcGxlLmNvbSIsImlhdCI6MTc1ODc4NjQxNywiZXhwIjoxNzYxMzc4NDE3fQ.ME0P3oEKEcP2bSEXIE3pXkWJdViLm1WohrNC42AJtXe1Im16UGupESLpWfgM75xO', '2025-10-25 07:46:57', 1, NULL, '2025-09-25 07:46:57', '2025-09-25 07:53:17'),
(28, 21, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6InJlY3J1aXRlckBleGFtcGxlLmNvbSIsImlhdCI6MTc1ODc4Njc5NywiZXhwIjoxNzYxMzc4Nzk3fQ.A4-kHxpzF4oX_28bO-oINc60zEfg2nhQTBrL6-wCLdnUwArqn1R1HX5YETwZIAij', '2025-10-25 07:53:17', 0, NULL, '2025-09-25 07:53:17', '2025-09-25 07:53:17'),
(29, 1, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImFkbWluQHJlY3J1aXRtZW50LmNvbSIsImlhdCI6MTc1ODc4NjgyOCwiZXhwIjoxNzYxMzc4ODI4fQ.XKV9_hXjxEN6Ubfu-Tm7TuWQu-QFt4sFb6OE_i1GhmonO7bc3fAW4CMzyTlqclSS', '2025-10-25 07:53:48', 1, NULL, '2025-09-25 07:53:48', '2025-09-30 07:41:03'),
(30, 5, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzU4Nzg2ODU5LCJleHAiOjE3NjEzNzg4NTl9.lRiJyof1ct5DyEEJ4wZud2QOmqZc6ZeeBKQuXpBk_EtiM5DpZO2JvYR4UW7VbU5C', '2025-10-25 07:54:19', 1, NULL, '2025-09-25 07:54:19', '2025-09-25 08:55:46'),
(31, 5, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzU4NzkwNTQ2LCJleHAiOjE3NjEzODI1NDZ9.jAJJ8CRMzIOp64mqoq9y3i64BZdn58I7yiWCLAvWeDdobgJqjwo0cJ8gbFmQxPbk', '2025-10-25 08:55:46', 1, NULL, '2025-09-25 08:55:46', '2025-09-25 09:24:07'),
(32, 5, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzU4NzkyMjQ3LCJleHAiOjE3NjEzODQyNDd9.p8uOIMmR7WrMJQk2QKvbO23-ZMqd2DbOtYmp4stpBXHy0CIrq6ih9KSfd_lPVSXH', '2025-10-25 09:24:07', 1, NULL, '2025-09-25 09:24:07', '2025-09-29 07:47:24'),
(33, 22, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6InVzZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTkxMjkwMDMsImV4cCI6MTc2MTcyMTAwM30.WzhI839vqnrNeWf1RewvG9MPEs2dOAnImLIRqJAmWO4fPIR-n3sqUfQXQ6jNZLGv', '2025-10-29 06:56:43', 1, NULL, '2025-09-29 06:56:43', '2025-09-29 06:58:07'),
(34, 22, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6InVzZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTkxMjkwODcsImV4cCI6MTc2MTcyMTA4N30.Sq_IhXCmztH1oBT_vwJ3p8rrUdcA9vld1qBgASBd90nkcOSK-WH5GSzQ7c0g6iAO', '2025-10-29 06:58:07', 1, NULL, '2025-09-29 06:58:07', '2025-09-29 09:19:48'),
(35, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1OTEzMTE4NCwiZXhwIjoxNzYxNzIzMTg0fQ.YZTeXxjji5eGh6Wsdtsiizqb4nCQqdT7PXX2Q8We6znoop-qQ8xNNWTV6FYklkiR', '2025-10-29 07:33:04', 1, NULL, '2025-09-29 07:33:04', '2025-09-29 07:48:33'),
(36, 5, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzU5MTMyMDQ0LCJleHAiOjE3NjE3MjQwNDR9.ZnxFKhO1yi2vcHkYRQtf5udh42bpshMada8hGTaLM7YpwdOZbwbiQoVrKeetlvOI', '2025-10-29 07:47:24', 1, NULL, '2025-09-29 07:47:24', '2025-10-01 07:31:27'),
(37, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1OTEzMjExMywiZXhwIjoxNzYxNzI0MTEzfQ.HGOO_Hoc4m_aGNELBTxUde2QOdKGTaESS7sIJpHUTuxMDF4C2g4jXjYglYgK8dIf', '2025-10-29 07:48:33', 1, NULL, '2025-09-29 07:48:33', '2025-10-02 07:14:14'),
(38, 22, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6InVzZXJAZXhhbXBsZS5jb20iLCJpYXQiOjE3NTkxMzc1ODgsImV4cCI6MTc2MTcyOTU4OH0.zHwyqN7-OX-O6rI3MVc_TN-4vrOC25DYbmN_4Bfelwkz7huUfx2J7zkGdwxawlg0', '2025-10-29 09:19:48', 0, NULL, '2025-09-29 09:19:48', '2025-09-29 09:19:48'),
(39, 1, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImFkbWluQHJlY3J1aXRtZW50LmNvbSIsImlhdCI6MTc1OTIxODA2MywiZXhwIjoxNzYxODEwMDYzfQ.wbVTLtdc8T0wYa_WIN4y_JUxVftzyF3x0N8aSv15K5SwrBUQa541Mn7lKgWOkFxI', '2025-10-30 07:41:03', 0, NULL, '2025-09-30 07:41:03', '2025-09-30 07:41:03'),
(40, 5, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzU5MzAzODg2LCJleHAiOjE3NjE4OTU4ODZ9.eUWB8Q1h6emOPvp9kWUcOt5kpnzin3b18YLSjpZ5q4X53fOBqlttbyTHYNSd3X95', '2025-10-31 07:31:26', 1, NULL, '2025-10-01 07:31:26', '2025-10-02 07:08:59'),
(41, 5, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzU5Mzg4OTM5LCJleHAiOjE3NjE5ODA5Mzl9.6uSq1ZqApDSrVFGbQkF0yXI1uz7Rc_gZ5KSa-a_icMX1sYIA9ZPcGBvhzAZ0iCU8', '2025-11-01 07:08:59', 1, NULL, '2025-10-02 07:08:59', '2025-10-02 07:26:44'),
(42, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1OTM4OTI1NCwiZXhwIjoxNzYxOTgxMjU0fQ.VDym0MJ8S-VgWXiReIUyToJ22UEF10E_K1SPL8tIdWJqGfJlkkppwl0NIkg7s7uF', '2025-11-01 07:14:14', 1, NULL, '2025-10-02 07:14:14', '2025-10-02 07:28:28'),
(43, 5, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwiaWF0IjoxNzU5MzkwMDA0LCJleHAiOjE3NjE5ODIwMDR9.vrCQW1qrg_BBhvmuuglLGsuBQFGcx0mOoQrM8GZn1v9-_J_DMyis65CFkvk5hjAf', '2025-11-01 07:26:44', 0, NULL, '2025-10-02 07:26:44', '2025-10-02 07:26:44'),
(44, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1OTM5MDEwOCwiZXhwIjoxNzYxOTgyMTA4fQ.lYduPF968hC4VXB2WtqIvEVAsrtqqmmOZ6gFShMTRj4t6oZrTm-G2mGF3RN535pT', '2025-11-01 07:28:28', 1, NULL, '2025-10-02 07:28:28', '2025-10-06 07:56:13'),
(45, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1OTczNzM3MywiZXhwIjoxNzYyMzI5MzczfQ.9UjaXc5ww6V1Jc2NFWqXQwp0_wjzm6VSye4KHOU9VWgpIw2woEgtRwviW3T7vs8c', '2025-11-05 07:56:13', 1, NULL, '2025-10-06 07:56:13', '2025-10-06 07:59:29'),
(46, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1OTczNzU2OSwiZXhwIjoxNzYyMzI5NTY5fQ.wFZVgTShvuVuFcxaVsCbxmiBzrbPF_B2TzflQJnY6P_9H4qs7rhyKsBkMjeSj1UR', '2025-11-05 07:59:29', 1, NULL, '2025-10-06 07:59:29', '2025-10-06 08:01:59'),
(47, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1OTczNzcxOSwiZXhwIjoxNzYyMzI5NzE5fQ.0YKXp3YzZ177np1eYXjTkTIEwGC2CGJNFTnmjRz6ioZhUXcNQ4iyJnpEASIKUzR4', '2025-11-05 08:01:59', 1, NULL, '2025-10-06 08:01:59', '2025-10-06 08:04:47'),
(48, 20, 'eyJhbGciOiJIUzM4NCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsInN1YiI6Im5ndXllbmIyMTEwMDUxQHN0dWRlbnQuY3R1LmVkdS52biIsImlhdCI6MTc1OTczNzg4NywiZXhwIjoxNzYyMzI5ODg3fQ.82vYgpVO5mFoO-IpjzhuLvLg_UbbOLQN1EVEQV7HRIpJKGsiMBTrTS5jCfIY-juk', '2025-11-05 08:04:47', 0, NULL, '2025-10-06 08:04:47', '2025-10-06 08:04:47');

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
  `verification_token_issued_at` timestamp NULL DEFAULT NULL,
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

INSERT INTO `users` (`id`, `email`, `password`, `first_name`, `last_name`, `phone_number`, `role`, `status`, `email_verified`, `verification_token`, `verification_token_issued_at`, `password_reset_token`, `password_reset_expires`, `avatar_url`, `last_login`, `company_id`, `created_at`, `updated_at`) VALUES
(1, 'admin@recruitment.com', '$2a$10$xrrBlgxjtduxrPzfLF.N/e54tE6g5EfPCNTYw4XxYacR5AQPY7EE.', 'Admin', 'System', NULL, 'ADMIN', 'ACTIVE', 1, NULL, NULL, NULL, NULL, NULL, '2025-09-30 07:41:02', NULL, '2025-09-16 08:20:18', '2025-09-30 07:41:02'),
(2, 'employer@techinnovate.com', '$2a$10$xrrBlgxjtduxrPzfLF.N/e54tE6g5EfPCNTYw4XxYacR5AQPY7EE.', 'John', 'Manager', NULL, 'EMPLOYER', 'ACTIVE', 1, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2025-09-16 08:20:18', '2025-09-22 06:52:01'),
(5, 'john.doe@example.com', '$2a$10$xrrBlgxjtduxrPzfLF.N/e54tE6g5EfPCNTYw4XxYacR5AQPY7EE.', 'John', 'Doe', NULL, 'APPLICANT', 'ACTIVE', 1, '3a9f742d-1c78-4dfa-963b-ce257875b043', NULL, NULL, NULL, NULL, '2025-10-02 07:26:44', NULL, '2025-09-16 09:16:08', '2025-10-02 07:26:44'),
(20, 'nguyenb2110051@student.ctu.edu.vn', '$2a$10$HJVAtYuDppZMk1wBjz5GYezvdcTXOYGE5dtk6alnq8RdAoeaRZzxG', 'Doan', 'Chi Nguyen', '0835886837', 'EMPLOYER', 'ACTIVE', 1, NULL, NULL, NULL, NULL, NULL, '2025-10-06 08:04:46', 5, '2025-09-24 07:51:53', '2025-10-06 08:04:46'),
(21, 'recruiter@example.com', '$2a$10$ioSYxLU09ewJui2Jxk80o.F75xMIQasS54zp3etV84a9tq3qEumla', 'Recruiter', 'User', '0900000001', 'RECRUITER', 'ACTIVE', 1, NULL, NULL, NULL, NULL, NULL, '2025-09-25 07:53:17', NULL, '2025-09-25 07:36:18', '2025-09-25 07:53:17'),
(22, 'user@example.com', '$2a$10$KFIuTaLlXo.Fcc93Gqyal.dnl1DeuB9KGt/SphVrWVFzT2a7OsJpy', 'First', 'Last', '0900000000', 'APPLICANT', 'ACTIVE', 1, NULL, NULL, NULL, NULL, NULL, '2025-09-29 09:19:48', NULL, '2025-09-29 06:56:05', '2025-09-29 09:19:48');

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
-- Chỉ mục cho bảng `application_timelines`
--
ALTER TABLE `application_timelines`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_app_id` (`application_id`),
  ADD KEY `fk_timeline_user` (`changed_by`);

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
  ADD KEY `idx_job_active_deadline_status` (`status`,`application_deadline`),
  ADD KEY `idx_job_location_ci` (`location`),
  ADD KEY `idx_created_by` (`created_by`),
  ADD KEY `idx_job_salary_min` (`salary_min`),
  ADD KEY `idx_job_salary_max` (`salary_max`),
  ADD KEY `idx_job_created_at` (`created_at`),
  ADD KEY `idx_job_published_at` (`published_at`);
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
-- Chỉ mục cho bảng `refresh_tokens`
--
ALTER TABLE `refresh_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `idx_refresh_token_token` (`token`),
  ADD KEY `idx_refresh_token_user` (`user_id`);

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
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `application_timelines`
--
ALTER TABLE `application_timelines`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `attachments`
--
ALTER TABLE `attachments`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `companies`
--
ALTER TABLE `companies`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

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
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

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
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT cho bảng `refresh_tokens`
--
ALTER TABLE `refresh_tokens`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=49;

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
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

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
-- Các ràng buộc cho bảng `application_timelines`
--
ALTER TABLE `application_timelines`
  ADD CONSTRAINT `fk_timeline_application` FOREIGN KEY (`application_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_timeline_user` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`) ON UPDATE CASCADE;

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
-- Các ràng buộc cho bảng `refresh_tokens`
--
ALTER TABLE `refresh_tokens`
  ADD CONSTRAINT `fk_refresh_token_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

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
