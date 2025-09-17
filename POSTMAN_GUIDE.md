# 📮 HƯỚNG DẪN TEST API BẰNG POSTMAN

## 🚀 Bước 1: Chuẩn bị

### 1.1 Đảm bảo Server đang chạy:
```bash
# Mở terminal và chạy:
cd "d:\xampp\htdocs\HTQL_TuyenDung\recruitment-system"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.1"
java -jar target/recruitment-system-1.0.0.jar
```
**Kiểm tra:** Server sẽ hiển thị `Tomcat started on port 8081`

### 1.2 Tải và cài đặt Postman:
- Tải từ: https://www.postman.com/downloads/
- Hoặc sử dụng Postman Web: https://web.postman.com/

---

## 📋 Bước 2: Tạo Collection trong Postman

### 2.1 Tạo New Collection:
1. Mở Postman
2. Click **"New"** → **"Collection"**
3. Đặt tên: `Recruitment System API`
4. Click **"Create"**

### 2.2 Thiết lập Environment (Tùy chọn):
1. Click **Environments** → **"Create Environment"**
2. Đặt tên: `Local Development`
3. Thêm biến:
   - `base_url`: `http://localhost:8081`
   - `token`: (để trống, sẽ set sau)
4. Click **"Save"**

---

## 🔐 Bước 3: Test API Authentication

### 3.1 **ĐĂNG KÝ USER MỚI**

**Tạo Request:**
1. Click **"Add Request"** trong Collection
2. Đặt tên: `Register New User`
3. Method: **POST**
4. URL: `http://localhost:8081/api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (chọn "raw" và "JSON"):**
```json
{
  "email": "john.doe@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "APPLICANT"
}
```

**Click "Send"**

**Kết quả mong đợi:**
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "type": "Bearer",
    "user": {
      "id": 5,
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "APPLICANT",
      "status": "PENDING"
    }
  }
}
```

### 3.2 **ĐĂNG NHẬP**

**Tạo Request:**
1. **Add Request**: `Login User`
2. Method: **POST**
3. URL: `http://localhost:8081/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Click "Send"**

**⚠️ Lưu JWT Token:**
1. Copy `token` từ response
2. Paste vào Environment variable `token`
3. Hoặc copy để dùng cho requests tiếp theo

---

## 🔒 Bước 4: Test Authenticated Endpoints

### 4.1 **Thiết lập Authorization Header:**

**Cách 1 - Thủ công:**
Trong mỗi request, thêm Header:
```
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

**Cách 2 - Dùng Environment:**
1. Trong Collection, click **"Authorization"**
2. Type: **Bearer Token**
3. Token: `{{token}}`

### 4.2 **TEST USER PROFILE**

**Tạo Request:**
1. **Add Request**: `Get Current User`
2. Method: **GET**
3. URL: `http://localhost:8081/api/user/profile`
4. Authorization: Bearer Token với JWT token

---

## 📊 Bước 5: Test Các Role khác nhau

### 5.1 **TẠO EMPLOYER:**
```json
{
  "email": "employer@company.com",
  "password": "employer123",
  "firstName": "Manager",
  "lastName": "Smith",
  "role": "EMPLOYER",
  "companyName": "Tech Company Inc"
}
```

### 5.2 **TẠO ADMIN:**
```json
{
  "email": "admin@system.com",
  "password": "admin123",
  "firstName": "System",
  "lastName": "Admin",
  "role": "ADMIN"
}
```

---

## 🎯 Bước 6: Test Specific Endpoints

### 6.1 **PUBLIC ENDPOINTS (Không cần token):**
- `GET /api/jobs/search` - Tìm kiếm việc làm
- `GET /api/companies/public` - Danh sách công ty public

### 6.2 **APPLICANT ENDPOINTS:**
- `GET /api/applicant/profile` - Xem profile
- `POST /api/applications/apply` - Nộp đơn ứng tuyển

### 6.3 **EMPLOYER ENDPOINTS:**
- `POST /api/employer/jobs` - Tạo tin tuyển dụng
- `GET /api/employer/applications` - Xem đơn ứng tuyển

### 6.4 **ADMIN ENDPOINTS:**
- `GET /api/admin/users` - Quản lý users
- `GET /api/admin/reports` - Báo cáo hệ thống

---

## 🔍 Bước 7: Test Cases thường gặp

### 7.1 **Test Validation Errors:**
**Request với email sai format:**
```json
{
  "email": "invalid-email",
  "password": "123456"
}
```
**Kết quả:** Status 400, validation error

### 7.2 **Test Authentication Errors:**
**Request không có token:**
- URL: `http://localhost:8081/api/user/profile`
- Không có Authorization header
**Kết quả:** Status 401 Unauthorized

### 7.3 **Test Authorization Errors:**
**APPLICANT truy cập ADMIN endpoint:**
- URL: `http://localhost:8081/api/admin/users`
- Token của APPLICANT
**Kết quả:** Status 403 Forbidden

---

## 📁 Bước 8: Export/Import Collection

### 8.1 **Export Collection:**
1. Click **"..."** bên cạnh Collection name
2. **Export**
3. Chọn định dạng **v2.1**
4. **Export** và lưu file

### 8.2 **Share Collection:**
```json
// File: recruitment-system-postman-collection.json
// Import vào Postman để sử dụng
```

---

## ⚡ Quick Test Commands

### Copy-paste nhanh vào Postman:

**1. Register:**
```
POST http://localhost:8081/api/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User",
  "role": "APPLICANT"
}
```

**2. Login:**
```
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

**3. Authenticated Request:**
```
GET http://localhost:8081/api/user/profile
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

---

## 🎉 HOÀN THÀNH!

Bây giờ bạn có thể test toàn bộ API của Recruitment System bằng Postman! 

**Ghi chú quan trọng:**
- Luôn kiểm tra server đang chạy trước khi test
- Copy JWT token sau mỗi lần login
- Kiểm tra status code và response format
- Test cả success và error cases