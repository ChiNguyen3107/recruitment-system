# 🚀 HƯỚNG DẪN TEST API BẰNG POSTMAN

## 📋 BƯỚC 1: CÀI ĐẶT VÀ THIẾT LẬP

### 1.1 Download và cài đặt Postman:
- Truy cập: https://www.postman.com/downloads/
- Download phiên bản phù hợp với hệ điều hành
- Cài đặt và đăng ký tài khoản (miễn phí)

### 1.2 Import Postman Collection:
1. Mở Postman
2. Click **"Import"** ở góc trên bên trái
3. Chọn **"Upload Files"** 
4. Browse và chọn file: `Recruitment_System_API.postman_collection.json`
5. Click **"Import"**

## 🔧 BƯỚC 2: CẤU HÌNH ENVIRONMENT

### 2.1 Tạo Environment mới:
1. Click **"Environments"** ở sidebar bên trái
2. Click **"+"** để tạo environment mới
3. Đặt tên: **"Recruitment System Local"**

### 2.2 Thiết lập Variables:
| Variable Name | Initial Value | Current Value |
|---------------|---------------|---------------|
| `base_url` | `http://localhost:8081` | `http://localhost:8081` |
| `jwt_token` | (để trống) | (để trống) |

### 2.3 Chọn Environment:
- Chọn **"Recruitment System Local"** từ dropdown ở góc phải trên

## 🌐 BƯỚC 3: KIỂM TRA SERVER

### 3.1 Đảm bảo Spring Boot đang chạy:
- Kiểm tra xem có thông báo: `Tomcat started on port 8081`
- Hoặc mở browser và truy cập: http://localhost:8081

### 3.2 Kiểm tra Database:
- XAMPP Control Panel → MySQL phải đang **Running**
- Database `recruitment_db` đã được tạo

## 🧪 BƯỚC 4: TEST CÁC API ENDPOINTS

### 4.1 Test 1: Đăng nhập với tài khoản Applicant

**Endpoint:** `POST {{base_url}}/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "email": "applicant@test.com",
  "password": "applicant123"
}
```

**Expected Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "applicant@test.com"
}
```

**Hành động sau khi test thành công:**
1. Copy giá trị `token` từ response
2. Vào **Environments** → **Recruitment System Local**
3. Paste token vào field `jwt_token`
4. Click **"Save"**

---

### 4.2 Test 2: Đăng nhập với tài khoản Employer

**Endpoint:** `POST {{base_url}}/auth/login`

**Body (raw JSON):**
```json
{
  "email": "employer@techinnovate.com",
  "password": "employer123"
}
```

---

### 4.3 Test 3: Đăng nhập với tài khoản Admin

**Endpoint:** `POST {{base_url}}/auth/login`

**Body (raw JSON):**
```json
{
  "email": "admin@recruitment.com",
  "password": "admin123"
}
```

---

### 4.4 Test 4: Lấy thông tin Profile (cần JWT token)

**Endpoint:** `GET {{base_url}}/auth/profile`

**Headers:**
```
Authorization: Bearer {{jwt_token}}
```

**Expected Response (200 OK):**
```json
{
  "id": 3,
  "email": "applicant@test.com",
  "firstName": "Nguyen",
  "lastName": "Van A",
  "role": "APPLICANT",
  "status": "ACTIVE"
}
```

---

### 4.5 Test 5: Đăng ký tài khoản mới

**Endpoint:** `POST {{base_url}}/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "email": "newuser@test.com",
  "password": "123456",
  "firstName": "New",
  "lastName": "User",
  "role": "APPLICANT"
}
```

**Expected Response (200 OK):**
```json
{
  "message": "User registered successfully",
  "email": "newuser@test.com"
}
```

## 🎯 BƯỚC 5: KIỂM TRA KẾT QUẢ

### 5.1 Test Cases thành công:
- ✅ Login với 3 tài khoản mẫu
- ✅ Nhận được JWT token hợp lệ
- ✅ Get profile với token authentication
- ✅ Register tài khoản mới

### 5.2 Test Cases lỗi (để kiểm tra error handling):

**Login với sai mật khẩu:**
```json
{
  "email": "applicant@test.com",
  "password": "wrongpassword"
}
```
*Expected: 401 Unauthorized*

**Get profile không có token:**
- Bỏ header `Authorization`
- *Expected: 401 Unauthorized*

**Register với email đã tồn tại:**
```json
{
  "email": "applicant@test.com",
  "password": "123456",
  "firstName": "Test",
  "lastName": "User",
  "role": "APPLICANT"
}
```
*Expected: 400 Bad Request*

## 🔍 BƯỚC 6: DEBUG VÀ TROUBLESHOOTING

### 6.1 Nếu gặp lỗi "Connection refused":
1. Kiểm tra Spring Boot có đang chạy không
2. Kiểm tra port 8081 có bị sử dụng bởi app khác không: `netstat -ano | findstr :8081`
3. Restart application nếu cần

### 6.2 Nếu gặp lỗi 500 Internal Server Error:
1. Kiểm tra MySQL có đang chạy không
2. Kiểm tra database `recruitment_db` có tồn tại không
3. Xem logs trong terminal của Spring Boot

### 6.3 Nếu JWT token không hoạt động:
1. Đảm bảo token được copy đầy đủ
2. Kiểm tra format header: `Bearer <token>`
3. Token có thể hết hạn sau 24h

## 📊 KẾT QUẢ MONG ĐỢI

Sau khi hoàn thành tất cả tests:
- **5/5 API endpoints** hoạt động đúng
- **JWT Authentication** working
- **Database integration** successful
- **Role-based responses** correct

## 🎉 COMPLETED!

**Bạn đã test thành công Recruitment System API!**

Bây giờ có thể:
1. Phát triển thêm endpoints
2. Integrate với frontend
3. Deploy to production

---

*💡 Tip: Sử dụng Postman Collection Runner để automate test suite*