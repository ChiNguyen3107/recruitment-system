# 🔒 HƯỚNG DẪN BẢO MẬT HỆ THỐNG

## 🚀 CÁC CẢI TIẾN BẢO MẬT ĐÃ THỰC HIỆN

### ✅ 1. JWT Secret Key Security
- **Trước**: Hardcode secret key trong `application.properties`
- **Sau**: Sử dụng environment variables với fallback
- **Cách sử dụng**: 
  ```bash
  export JWT_SECRET="YourVerySecureJWTSecretKeyThatIsAtLeast32CharactersLong"
  ```

### ✅ 2. Loại bỏ Debug Endpoints
- **Đã xóa**: `DebugController.java` - có thể lộ thông tin nhạy cảm
- **Kết quả**: Không còn endpoint `/debug/**` public

### ✅ 3. Rate Limiting
- **Thêm**: Bucket4j để chống brute force attacks
- **Cấu hình**:
  - Login: 5 lần/5 phút
  - Register: 3 lần/giờ
- **Dependencies**: `bucket4j-core:7.6.0`

### ✅ 4. Error Handling Cải tiến
- **Thêm**: `GlobalExceptionHandler` để xử lý lỗi thống nhất
- **Không leak thông tin**: Thông báo lỗi generic cho user
- **Logging**: Ghi log chi tiết cho admin

### ✅ 5. Audit Logging
- **Thêm**: `AuditLogger` để ghi lại các hoạt động bảo mật
- **Theo dõi**: Login/Logout, Registration, Token refresh, Rate limit
- **Format**: `[AUDIT] timestamp ACTION - Details`

### ✅ 6. Security Headers
- **Thêm**: `SecurityHeadersFilter` với các headers:
  - `X-Frame-Options: DENY`
  - `X-Content-Type-Options: nosniff`
  - `X-XSS-Protection: 1; mode=block`
  - `Strict-Transport-Security`
  - `Content-Security-Policy`
  - `Referrer-Policy`
  - `Permissions-Policy`

### ✅ 7. CORS Configuration Cải tiến
- **Trước**: Cho phép tất cả localhost
- **Sau**: Chỉ cho phép origins được cấu hình
- **Cấu hình**: `CORS_ALLOWED_ORIGINS` environment variable

## 🔧 CÁCH CẤU HÌNH PRODUCTION

### 1. Tạo file `.env` từ template
```bash
cp env.example .env
```

### 2. Cấu hình environment variables
```bash
# JWT Security (BẮT BUỘC)
export JWT_SECRET="YourProductionSecretKeyAtLeast32CharactersLong"

# CORS Configuration
export CORS_ALLOWED_ORIGINS="https://yourdomain.com,https://app.yourdomain.com"

# Rate Limiting (tùy chọn)
export RATE_LIMIT_LOGIN=10
export RATE_LIMIT_LOGIN_WINDOW=300
export RATE_LIMIT_REGISTER=5
export RATE_LIMIT_REGISTER_WINDOW=3600

# Security Headers
export SECURITY_HEADERS_ENABLED=true
```

### 3. Database Security
```bash
# Thay đổi password mặc định
export DB_PASSWORD="YourSecureDatabasePassword"
```

## 🧪 KIỂM THỬ BẢO MẬT

### Test Rate Limiting
```bash
# Test login rate limit
for i in {1..6}; do
  curl -X POST http://localhost:8081/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"wrongpassword"}'
done
```

### Test Security Headers
```bash
curl -I http://localhost:8081/api/auth/me
# Kiểm tra các headers: X-Frame-Options, X-Content-Type-Options, etc.
```

### Test CORS
```bash
curl -X OPTIONS http://localhost:8081/api/auth/login \
  -H "Origin: https://malicious.com" \
  -H "Access-Control-Request-Method: POST"
```

## 📊 MONITORING & AUDIT

### 1. Audit Logs
```bash
# Theo dõi audit logs
tail -f logs/application.log | grep "\[AUDIT\]"
```

### 2. Rate Limit Monitoring
```bash
# Theo dõi rate limit violations
tail -f logs/application.log | grep "Rate limit exceeded"
```

### 3. Failed Login Attempts
```bash
# Theo dõi đăng nhập thất bại
tail -f logs/application.log | grep "LOGIN_FAILURE"
```

## 🚨 CẢNH BÁO BẢO MẬT

### ⚠️ QUAN TRỌNG
1. **KHÔNG BAO GIỜ** commit file `.env` vào git
2. **THAY ĐỔI** JWT_SECRET cho môi trường production
3. **KIỂM TRA** CORS_ALLOWED_ORIGINS thường xuyên
4. **THEO DÕI** audit logs để phát hiện tấn công
5. **CẬP NHẬT** dependencies thường xuyên

### 🔍 Security Checklist
- [ ] JWT secret key được bảo mật
- [ ] Rate limiting hoạt động
- [ ] Security headers được thêm
- [ ] CORS được cấu hình đúng
- [ ] Audit logging hoạt động
- [ ] Error handling không leak thông tin
- [ ] Debug endpoints đã bị xóa

## 📞 LIÊN HỆ HỖ TRỢ

Nếu gặp vấn đề về bảo mật, vui lòng:
1. Kiểm tra logs để xác định nguyên nhân
2. Xem lại cấu hình environment variables
3. Test lại các endpoint với các test cases đã cung cấp
4. Liên hệ team development để được hỗ trợ

---
**Lưu ý**: Tài liệu này chứa thông tin nhạy cảm về bảo mật. Chỉ chia sẻ với team có thẩm quyền.








