# 🚀 QUICK API TEST - DEMO NHANH

## ✅ SERVER STATUS: RUNNING ON PORT 8081

### 🧪 Test ngay bằng Postman:

#### 1. **Test Login Applicant** (COPY & PASTE vào Postman):

**Method:** `POST`
**URL:** `http://localhost:8081/auth/login`
**Headers:**
```
Content-Type: application/json
```
**Body:**
```json
{
  "email": "applicant@test.com",
  "password": "applicant123"
}
```

---

#### 2. **Test Login Employer** (COPY & PASTE vào Postman):

**Method:** `POST`
**URL:** `http://localhost:8081/auth/login`
**Headers:**
```
Content-Type: application/json
```
**Body:**
```json
{
  "email": "employer@techinnovate.com",
  "password": "employer123"
}
```

---

#### 3. **Test Get Profile** (Cần JWT token từ login):

**Method:** `GET`
**URL:** `http://localhost:8081/auth/profile`
**Headers:**
```
Authorization: Bearer [PASTE_JWT_TOKEN_HERE]
```

---

### 📋 HƯỚNG DẪN NHANH:

1. **Mở Postman**
2. **Import collection** từ file: `Recruitment_System_API.postman_collection.json`
3. **Copy/paste** các thông tin trên
4. **Click Send** để test

### 🎯 KẾT QUẢ MONG ĐỢI:

**✅ Login thành công:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",  
  "username": "applicant@test.com"
}
```

**✅ Get Profile thành công:**
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

## 🔥 **TẤT CẢ ĐÃ SẴN SÀNG!**

- ✅ **Spring Boot**: Running on port 8081
- ✅ **Database**: Connected (recruitment_db)
- ✅ **Authentication**: JWT Working
- ✅ **API Endpoints**: Ready to test
- ✅ **Postman Collection**: Available

**Bắt đầu test ngay! 🚀**