# Postman Test Cases cho Recruitment System

## 🔐 Authentication Tests

### TC001: Đăng nhập thành công
- **Request**: POST /api/auth/login với credentials hợp lệ
- **Expected**: 200 OK, trả về token và thông tin user
- **Test Script**:
```javascript
pm.test("Login successful", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.token).to.exist;
    pm.environment.set("authToken", "Bearer " + jsonData.token);
});
```

### TC002: Đăng nhập thất bại
- **Request**: POST /api/auth/login với credentials sai
- **Expected**: 401 Unauthorized

## 👤 Profile Management Tests

### TC003: Lấy profile hiện tại - thành công
- **Request**: GET /api/profile với valid token
- **Expected**: 200 OK, trả về thông tin profile
- **Test Script**:
```javascript
pm.test("Get profile successful", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.user).to.exist;
});
```

### TC004: Cập nhật profile - thành công
- **Request**: PUT /api/profile với data hợp lệ
- **Expected**: 200 OK, profile được cập nhật
- **Test Script**:
```javascript
pm.test("Update profile successful", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.message).to.include("success");
});
```

### TC005: Truy cập profile mà không có token
- **Request**: GET /api/profile không có Authorization header
- **Expected**: 401 Unauthorized

## 💼 Job Posting Tests (EMPLOYER role required)

### TC006: Tạo job posting - thành công
- **Request**: POST /api/jobs với EMPLOYER token
- **Expected**: 201 Created, job được tạo
- **Test Script**:
```javascript
pm.test("Create job posting successful", function () {
    pm.response.to.have.status(201);
    var jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.environment.set("jobId", jsonData.id);
});
```

### TC007: Tạo job posting với USER role
- **Request**: POST /api/jobs với USER token
- **Expected**: 403 Forbidden

### TC008: Cập nhật job posting - thành công
- **Request**: PUT /api/jobs/{id} với EMPLOYER token
- **Expected**: 200 OK, job được cập nhật

### TC009: Xóa job posting - thành công
- **Request**: DELETE /api/jobs/{id} với EMPLOYER token
- **Expected**: 200 OK, job được xóa

### TC010: Lấy danh sách job của employer
- **Request**: GET /api/jobs/my-jobs với EMPLOYER token
- **Expected**: 200 OK, danh sách jobs với pagination

## 🔍 Search Tests (Public APIs)

### TC011: Tìm kiếm job posting
- **Request**: GET /api/jobs/search với parameters
- **Expected**: 200 OK, danh sách jobs phù hợp
- **Test Script**:
```javascript
pm.test("Search jobs successful", function () {
    pm.response.to.have.status(200);
    var jsonData = pm.response.json();
    pm.expect(jsonData.content).to.be.an('array');
    pm.expect(jsonData.totalElements).to.be.a('number');
});
```

### TC012: Xem chi tiết job posting
- **Request**: GET /api/jobs/{id}
- **Expected**: 200 OK, thông tin chi tiết job

## 🚫 Error Handling Tests

### TC013: Truy cập endpoint không tồn tại
- **Request**: GET /api/nonexistent
- **Expected**: 404 Not Found

### TC014: Gửi request với data không hợp lệ
- **Request**: POST /api/jobs với missing required fields
- **Expected**: 400 Bad Request với error messages

### TC015: Truy cập resource của user khác
- **Request**: PUT /api/jobs/{id} với id không thuộc về user
- **Expected**: 403 Forbidden hoặc 404 Not Found

## 📝 Sample Test Data

### User Accounts để test:
```json
// Admin User
{
    "email": "admin@example.com",
    "password": "admin123"
}

// Employer User
{
    "email": "employer@example.com", 
    "password": "employer123"
}

// Regular User
{
    "email": "user@example.com",
    "password": "user123"
}
```

### Sample Job Posting Data:
```json
{
    "title": "Senior Java Developer",
    "description": "Tuyển dụng Senior Java Developer có kinh nghiệm 3+ năm",
    "requirements": "- 3+ năm kinh nghiệm Java\n- Thành thạo Spring Boot\n- Kinh nghiệm MySQL",
    "benefits": "- Lương competitive\n- Bảo hiểm đầy đủ\n- Môi trường thân thiện",
    "salary": "20000000",
    "location": "TP. Hồ Chí Minh",
    "jobType": "FULL_TIME",
    "expiryDate": "2025-12-31"
}
```

## 🎯 Testing Checklist

- [ ] Authentication flows (login/logout)
- [ ] Authorization (role-based access)
- [ ] Profile CRUD operations
- [ ] Job posting CRUD operations (EMPLOYER only)
- [ ] Search functionality
- [ ] Pagination
- [ ] Error handling
- [ ] Input validation
- [ ] Security (unauthorized access)
- [ ] Performance (response times)