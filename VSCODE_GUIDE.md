# Hướng dẫn chạy Spring Boot trong VS Code

## 1. Cài đặt Extension Pack for Java:
- Mở VS Code
- Vào Extensions (Ctrl+Shift+X)
- Tìm "Extension Pack for Java" và cài đặt

## 2. Mở project:
- File → Open Folder → Chọn thư mục `recruitment-system`

## 3. Chạy application:
### Cách 1: Sử dụng VS Code
- Mở file `RecruitmentSystemApplication.java`
- Click chuột phải → "Run Java"
- Hoặc nhấn F5

### Cách 2: Sử dụng Terminal tích hợp
- Terminal → New Terminal
- Chạy lệnh: `./mvnw spring-boot:run` (Linux/Mac)
- Hoặc: `mvnw.cmd spring-boot:run` (Windows)

## 4. Kiểm tra kết quả:
Application sẽ chạy trên `http://localhost:8080`

## 5. Test API endpoints:
Sử dụng Postman hoặc Thunder Client extension trong VS Code để test các endpoint đã được tạo sẵn.

### Endpoints có sẵn:
- POST `/api/auth/login` - Đăng nhập
- POST `/api/auth/register` - Đăng ký
- GET `/api/auth/profile` - Lấy thông tin profile (cần token)

### Test đăng nhập:
```json
POST http://localhost:8080/api/auth/login
{
  "email": "applicant@test.com",
  "password": "applicant123"
}
```

Nếu thành công, bạn sẽ nhận được JWT token để sử dụng cho các API khác.