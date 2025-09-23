# 📮 HƯỚNG DẪN SỬ DỤNG POSTMAN COLLECTION

## 🚀 CÁCH IMPORT VÀ SỬ DỤNG

### 1. Import Collection vào Postman
1. Mở Postman
2. Click **Import** (góc trái trên)
3. Chọn **Upload Files**
4. Chọn file `Recruitment_System_Auth_Collection.json`
5. Click **Import**

### 2. Tạo Environment Variables
1. Click **Environments** tab (trái màn hình)
2. Click **Create Environment**
3. Đặt tên: `Recruitment System - Local`
4. Thêm các variables:

| Variable | Initial Value | Current Value |
|----------|---------------|---------------|
| `baseUrl` | `http://localhost:8081` | `http://localhost:8081` |
| `accessToken` | (để trống) | (sẽ được set tự động) |
| `refreshToken` | (để trống) | (sẽ được set tự động) |
| `userEmail` | (để trống) | (sẽ được set tự động) |
| `userRole` | (để trống) | (sẽ được set tự động) |
| `employerAccessToken` | (để trống) | (sẽ được set tự động) |
| `employerRefreshToken` | (để trống) | (sẽ được set tự động) |
| `employerEmail` | (để trống) | (sẽ được set tự động) |
| `employerRole` | (để trống) | (sẽ được set tự động) |

5. Click **Save**
6. Chọn environment này trong dropdown (góc phải trên)

## 📁 CẤU TRÚC COLLECTION

### 🔐 **Auth Folder**
- **Register - Applicant**: Đăng ký tài khoản ứng viên
- **Register - Employer**: Đăng ký tài khoản nhà tuyển dụng
- **Login - Applicant**: Đăng nhập ứng viên
- **Login - Employer**: Đăng nhập nhà tuyển dụng
- **Login - Wrong Password**: Test rate limiting
- **Refresh Token**: Làm mới token
- **Logout**: Đăng xuất

### 👤 **User Folder**
- **Get Current User Info**: Lấy thông tin user hiện tại

### 🛡️ **Protected API Folder**
- **Test Protected Endpoint - Jobs**: Test endpoint cần auth
- **Test Protected Endpoint - Without Token**: Test không có token
- **Test Protected Endpoint - Invalid Token**: Test token không hợp lệ
- **Test Public Endpoint**: Test endpoint public

### 🔒 **Security Tests Folder**
- **Test Rate Limiting**: Test giới hạn số lần đăng nhập
- **Test JWT Token Structure**: Test cấu trúc JWT

## 🧪 QUY TRÌNH TEST

### **Bước 1: Khởi động Server**
```bash
cd recruitment-system
mvn spring-boot:run
```

### **Bước 2: Test Authentication Flow**
1. **Register** → Chạy "Register - Applicant"
2. **Login** → Chạy "Login - Applicant"
3. **Get User Info** → Chạy "Get Current User Info"
4. **Test Protected API** → Chạy "Test Protected Endpoint - Jobs"
5. **Refresh Token** → Chạy "Refresh Token"
6. **Logout** → Chạy "Logout"

### **Bước 3: Test Security Features**
1. **Rate Limiting** → Chạy "Login - Wrong Password" nhiều lần
2. **Token Validation** → Chạy "Test JWT Token Structure"
3. **Authorization** → Test các endpoint với token khác nhau

## ✅ TEST SCRIPTS TỰ ĐỘNG

### **Test Cases được thực hiện tự động:**

#### **🔐 Authentication Tests**
- ✅ Status code = 200
- ✅ Response có `accessToken`
- ✅ `accessToken` có dạng JWT (3 phần)
- ✅ Response có `refreshToken`
- ✅ Response có thông tin user
- ✅ Tự động lưu tokens vào environment

#### **🛡️ Authorization Tests**
- ✅ Protected endpoint trả về 200 với token hợp lệ
- ✅ Protected endpoint trả về 401 không có token
- ✅ Protected endpoint trả về 401 với token không hợp lệ
- ✅ Public endpoint trả về 200 không cần token

#### **🔒 Security Tests**
- ✅ Rate limiting hoạt động (status 429)
- ✅ JWT token có cấu trúc đúng (3 phần)
- ✅ JWT payload chứa thông tin cần thiết (sub, exp, iat)
- ✅ Token refresh tạo token mới
- ✅ Logout xóa tokens khỏi environment

## 📊 MONITORING KẾT QUẢ

### **1. Xem Test Results**
- Click **Test Results** tab trong Postman
- Xem các test cases đã pass/fail
- Đọc error messages nếu có

### **2. Xem Console Logs**
- Click **Console** tab (dưới cùng)
- Xem các log messages từ test scripts
- Debug nếu có vấn đề

### **3. Xem Environment Variables**
- Click **Environments** tab
- Xem các tokens đã được lưu
- Verify tokens có giá trị đúng

## 🚨 TROUBLESHOOTING

### **Lỗi thường gặp:**

#### **1. Connection Refused**
```
Error: connect ECONNREFUSED 127.0.0.1:8081
```
**Giải pháp**: Đảm bảo server đang chạy trên port 8081

#### **2. 401 Unauthorized**
```
Status code is 401
```
**Giải pháp**: 
- Kiểm tra token có được lưu trong environment không
- Chạy lại login để lấy token mới
- Kiểm tra token có hết hạn không

#### **3. 429 Too Many Requests**
```
Status code is 429
```
**Giải pháp**: Đây là rate limiting hoạt động bình thường, đợi vài phút rồi thử lại

#### **4. Environment Variables không được set**
```
No access token found in environment variables
```
**Giải pháp**: 
- Chạy login request trước
- Kiểm tra environment đã được chọn chưa
- Kiểm tra test scripts có chạy không

## 🔄 AUTOMATION

### **Chạy toàn bộ Collection:**
1. Click **Collections** tab
2. Click vào collection "Recruitment System - Authentication API"
3. Click **Run** button
4. Chọn environment
5. Click **Run Recruitment System - Authentication API**

### **Chạy specific folder:**
1. Click vào folder (Auth, User, Protected API, Security Tests)
2. Click **Run** button
3. Chọn environment
4. Click **Run [Folder Name]**

## 📈 PERFORMANCE MONITORING

### **Response Time Tests:**
- Tất cả requests đều có test kiểm tra response time < 5000ms
- Xem kết quả trong **Test Results** tab

### **JSON Response Validation:**
- Tất cả responses đều được validate là JSON format
- Kiểm tra structure của response data

## 🎯 KẾT QUẢ MONG ĐỢI

### **Khi tất cả tests pass:**
- ✅ 100% test cases thành công
- ✅ Tokens được lưu và sử dụng đúng
- ✅ Rate limiting hoạt động
- ✅ JWT structure hợp lệ
- ✅ Authorization hoạt động chính xác

### **Security Score:**
- **Authentication**: ✅ Hoàn hảo
- **Authorization**: ✅ Hoàn hảo  
- **Rate Limiting**: ✅ Hoạt động
- **JWT Security**: ✅ Bảo mật
- **Error Handling**: ✅ Không leak thông tin

---
**Lưu ý**: Collection này được thiết kế để test toàn diện hệ thống authentication. Hãy chạy theo thứ tự để có kết quả chính xác nhất.













