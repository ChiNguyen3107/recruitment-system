# 🎉 DATABASE IMPORT THÀNH CÔNG!

## ✅ Đã hoàn thành:
- ✅ Database `recruitment_db` đã được tạo thành công
- ✅ Tất cả 16 bảng đã được tạo
- ✅ Dữ liệu mẫu đã được import

## 📊 Thông tin đăng nhập mẫu:
- **Admin**: `admin@recruitment.com` / `admin123`
- **Employer**: `employer@techinnovate.com` / `employer123`  
- **Applicant**: `applicant@test.com` / `applicant123`

## 🚀 Bước tiếp theo để chạy Spring Boot:

### 1. Cài đặt Maven (nếu chưa có):
```bash
# Download Maven từ: https://maven.apache.org/download.cgi
# Hoặc cài qua Chocolatey:
choco install maven

# Hoặc cài qua Scoop:
scoop install maven
```

### 2. Cấu hình database connection:
File `src/main/resources/application.properties` đã được cấu hình sẵn:
```properties
spring.datasource.username=root
spring.datasource.password=
```

### 3. Chạy Spring Boot application:
```bash
# Compile project
mvn clean compile

# Chạy application
mvn spring-boot:run
```

### 4. Test API với Postman:

**Đăng ký tài khoản mới:**
```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "newuser@test.com",
  "password": "123456",
  "firstName": "New",
  "lastName": "User",
  "role": "APPLICANT"
}
```

**Đăng nhập:**
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "applicant@test.com",
  "password": "applicant123"
}
```

## 📋 Cấu trúc Database đã tạo:

### Bảng chính:
- `users` - Người dùng hệ thống
- `companies` - Công ty
- `profiles` - Hồ sơ ứng viên
- `job_postings` - Tin tuyển dụng
- `applications` - Đơn ứng tuyển

### Bảng phụ trợ:
- `skills` - Kỹ năng
- `attachments` - File đính kèm
- `educations` - Học vấn
- `work_experiences` - Kinh nghiệm làm việc
- `interview_schedules` - Lịch phỏng vấn
- `messages` - Tin nhắn
- `notifications` - Thông báo
- `status_logs` - Lịch sử thay đổi trạng thái
- `oauth_accounts` - Tài khoản OAuth

## 🎯 Tính năng có thể test:
1. Đăng ký/đăng nhập
2. Quản lý hồ sơ ứng viên
3. Tạo tin tuyển dụng (cho employer)
4. Nộp đơn ứng tuyển
5. Quản lý trạng thái đơn ứng tuyển

Project đã sẵn sàng để phát triển! 🚀