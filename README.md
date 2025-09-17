# Recruitment System - Hệ thống Quản lý Tuyển dụng

## Mô tả
Hệ thống quản lý tuyển dụng được xây dựng với Spring Boot 3.x, Java 17, và MySQL. Hệ thống hỗ trợ nhiều vai trò người dùng và cung cấp các tính năng quản lý tin tuyển dụng, hồ sơ ứng viên, và quy trình tuyển dụng.

## Công nghệ sử dụng
- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Security**: JWT Authentication
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA
- **Build Tool**: Maven
- **IDE**: VS Code / IntelliJ IDEA

## Cài đặt và Chạy dự án

### 1. Yêu cầu hệ thống
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### 2. Cài đặt database
```sql
CREATE DATABASE recruitment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Cấu hình database
Chỉnh sửa file `src/main/resources/application.properties`:
```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Cấu hình Email (tuỳ chọn)
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 5. Chạy ứng dụng
```bash
# Clone project
cd recruitment-system

# Build và chạy
mvn clean install
mvn spring-boot:run
```

Ứng dụng sẽ chạy trên: http://localhost:8080

## Cấu trúc dự án
```
src/main/java/com/recruitment/system/
├── config/          # Cấu hình Spring Security, JWT
├── controller/      # REST API Controllers
├── dto/            # Data Transfer Objects
│   ├── request/    # Request DTOs
│   └── response/   # Response DTOs
├── entity/         # JPA Entities
├── enums/          # Enums
├── repository/     # JPA Repositories
├── service/        # Business Logic Services
└── RecruitmentSystemApplication.java
```

## API Endpoints

### Authentication
- **POST** `/api/auth/register` - Đăng ký tài khoản
- **POST** `/api/auth/login` - Đăng nhập

### Jobs (Public)
- **GET** `/api/jobs/search` - Tìm kiếm việc làm
- **GET** `/api/jobs/public/{id}` - Xem chi tiết việc làm

### Employer APIs
- **POST** `/api/employer/jobs` - Tạo tin tuyển dụng
- **PUT** `/api/employer/jobs/{id}` - Cập nhật tin tuyển dụng
- **GET** `/api/employer/jobs` - Danh sách tin tuyển dụng của công ty

### Applicant APIs
- **POST** `/api/applicant/applications` - Nộp đơn ứng tuyển
- **GET** `/api/applicant/applications` - Danh sách đơn ứng tuyển
- **PUT** `/api/applicant/profile` - Cập nhật hồ sơ

## Vai trò người dùng

### 1. Admin
- Quản lý toàn bộ hệ thống
- Quản lý người dùng, công ty
- Xem báo cáo thống kê

### 2. Employer (Nhà tuyển dụng)
- Tạo và quản lý tin tuyển dụng
- Xem và quản lý đơn ứng tuyển
- Lên lịch phỏng vấn

### 3. Recruiter (Nhân viên tuyển dụng)
- Tương tự Employer nhưng quyền hạn có thể bị giới hạn

### 4. Applicant (Ứng viên)
- Tạo và quản lý hồ sơ cá nhân
- Tìm kiếm và ứng tuyển việc làm
- Theo dõi trạng thái đơn ứng tuyển

### 5. Guest
- Xem tin tuyển dụng công khai
- Tìm kiếm việc làm

## Testing với Postman

### 1. Đăng ký tài khoản
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "employer@test.com",
  "password": "123456",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "0987654321",
  "role": "EMPLOYER",
  "companyName": "Tech Company",
  "companyDescription": "Leading tech company"
}
```

### 2. Đăng nhập
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "employer@test.com",
  "password": "123456"
}
```

### 3. Sử dụng JWT Token
Sau khi đăng nhập thành công, thêm header cho các request khác:
```
Authorization: Bearer your_jwt_token_here
```

## Cơ sở dữ liệu

### Tạo tables cần thiết
```sql
USE recruitment_db;

-- Tạo bảng companies trước
CREATE TABLE companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    business_license VARCHAR(100),
    tax_code VARCHAR(50),
    website VARCHAR(255),
    industry VARCHAR(100),
    company_size VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    phone_number VARCHAR(20),
    contact_email VARCHAR(255),
    logo_url VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tạo bảng users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(15),
    role ENUM('ADMIN', 'EMPLOYER', 'RECRUITER', 'APPLICANT', 'GUEST') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'PENDING', 'SUSPENDED', 'DELETED') DEFAULT 'PENDING',
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires TIMESTAMP NULL,
    avatar_url VARCHAR(500),
    last_login TIMESTAMP NULL,
    company_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Các bảng khác sẽ được tạo tự động bởi JPA
```

## Mở rộng

### Thêm tính năng mới
1. Tạo Entity trong package `entity`
2. Tạo Repository trong package `repository`
3. Tạo Service trong package `service`
4. Tạo Controller trong package `controller`
5. Tạo DTO trong package `dto`

### Cấu hình bổ sung
- Email templates trong `src/main/resources/templates`
- File upload trong `uploads/` directory
- Logging configuration trong `application.properties`

## Hỗ trợ
Nếu gặp vấn đề khi setup hoặc chạy dự án, vui lòng kiểm tra:
1. Java version (phải là 17+)
2. Database connection
3. Port 8080 có bị conflict không
4. Maven dependencies đã download đầy đủ chưa

## License
This project is licensed under the MIT License.