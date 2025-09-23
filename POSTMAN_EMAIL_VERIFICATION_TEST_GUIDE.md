# Hướng dẫn Test Email Verification với Postman

## Cài đặt Postman Collection

1. Import file `Recruitment System API.postman_collection.json` vào Postman
2. Đảm bảo server đang chạy trên port 8081
3. Cấu hình Environment Variables nếu cần

## Test Flow Hoàn Chỉnh

### Bước 1: Đăng ký tài khoản mới

**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "email": "testuser@example.com",
  "password": "Password@123",
  "firstName": "Test",
  "lastName": "User",
  "role": "APPLICANT"
}
```

**Expected Response:**
- `success: true`
- `user.emailVerified: false`
- Email xác minh được gửi tự động

### Bước 2: Kiểm tra trạng thái email verification

**Endpoint:** `GET /api/debug/auth/email-status/testuser@example.com`

**Expected Response:**
```json
{
  "userFound": true,
  "email": "testuser@example.com",
  "emailVerified": false,
  "canLogin": false,
  "hasVerificationToken": true,
  "verificationToken": "uuid-token-here"
}
```

### Bước 3: Lấy verification token

**Endpoint:** `GET /api/debug/auth/verification-token/testuser@example.com`

**Expected Response:**
```json
{
  "userFound": true,
  "email": "testuser@example.com",
  "emailVerified": false,
  "verificationToken": "uuid-token-here",
  "hasVerificationToken": true,
  "tokenExpired": false
}
```

**Lưu ý:** Copy token này để sử dụng ở bước tiếp theo.

### Bước 4: Thử đăng nhập trước khi xác minh (sẽ fail)

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "testuser@example.com",
  "password": "Password@123"
}
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Vui lòng xác minh email trước khi đăng nhập. Kiểm tra hộp thư của bạn hoặc yêu cầu gửi lại email xác minh."
}
```

### Bước 5: Xác minh email

**Endpoint:** `POST /api/auth/verify-email`

**Request Body:**
```json
{
  "token": "uuid-token-from-step-3"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Email đã được xác minh thành công",
  "data": null
}
```

### Bước 6: Kiểm tra trạng thái sau xác minh

**Endpoint:** `GET /api/debug/auth/email-status/testuser@example.com`

**Expected Response:**
```json
{
  "userFound": true,
  "email": "testuser@example.com",
  "emailVerified": true,
  "canLogin": true,
  "hasVerificationToken": false
}
```

### Bước 7: Đăng nhập thành công

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "testuser@example.com",
  "password": "Password@123"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "jwt-token",
    "refreshToken": "refresh-token",
    "user": {
      "emailVerified": true,
      ...
    }
  }
}
```

## Test Cases Khác

### Test Resend Verification Email

**Endpoint:** `POST /api/auth/resend-verification`

**Request Body:**
```json
{
  "email": "testuser@example.com"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Email xác minh đã được gửi lại",
  "data": null
}
```

### Test Invalid Token

**Endpoint:** `POST /api/auth/verify-email`

**Request Body:**
```json
{
  "token": "invalid-token"
}
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Token xác minh không hợp lệ hoặc đã hết hạn"
}
```

### Test Already Verified Email

**Endpoint:** `POST /api/auth/resend-verification`

**Request Body:**
```json
{
  "email": "already-verified@example.com"
}
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Email này đã được xác minh"
}
```

## Environment Variables

Tạo environment variables trong Postman:

- `baseUrl`: `http://localhost:8081`
- `accessToken`: (sẽ được set tự động sau khi login)
- `refreshToken`: (sẽ được set tự động sau khi login)
- `verificationToken`: (copy từ debug endpoint)

## Scripts Tự Động

### Pre-request Script cho Login

```javascript
// Set email và password từ environment variables
pm.request.body.raw = JSON.stringify({
    "email": pm.environment.get("testEmail") || "testuser@example.com",
    "password": pm.environment.get("testPassword") || "Password@123"
});
```

### Test Script cho Login

```javascript
if (pm.response.code === 200) {
    const response = pm.response.json();
    if (response.success && response.data) {
        pm.environment.set("accessToken", response.data.accessToken);
        pm.environment.set("refreshToken", response.data.refreshToken);
    }
}
```

### Test Script cho Verify Email

```javascript
if (pm.response.code === 200) {
    const response = pm.response.json();
    pm.test("Email verification successful", function () {
        pm.expect(response.success).to.be.true;
    });
}
```

## Collection Runner

Để test tự động toàn bộ flow:

1. Tạo Collection Runner
2. Thêm các request theo thứ tự:
   - Register
   - Get Verification Token
   - Verify Email
   - Login
   - Get User Info

3. Cấu hình delay giữa các request (1-2 giây)

## Troubleshooting

### Lỗi thường gặp:

1. **"User not found"**
   - Kiểm tra email đã đăng ký chưa
   - Chạy Register trước

2. **"Token không hợp lệ"**
   - Token đã hết hạn (24h)
   - Token đã được sử dụng
   - Copy token không đúng

3. **"Email đã được xác minh"**
   - User đã verify rồi
   - Tạo user mới để test

4. **Rate limit exceeded**
   - Đợi 1 giờ hoặc thay đổi IP
   - Kiểm tra cấu hình rate limiting

### Debug Commands:

```bash
# Kiểm tra user trong database
curl http://localhost:8081/api/debug/auth/user/testuser@example.com

# Kiểm tra verification token
curl http://localhost:8081/api/debug/auth/verification-token/testuser@example.com

# Kiểm tra trạng thái email
curl http://localhost:8081/api/debug/auth/email-status/testuser@example.com
```

## Kết quả mong đợi

Sau khi hoàn thành test flow:
- ✅ User đăng ký thành công với `emailVerified: false`
- ✅ Email xác minh được gửi tự động
- ✅ Login bị từ chối khi chưa verify
- ✅ Verify email thành công
- ✅ Login thành công sau khi verify
- ✅ Token verification bị xóa sau khi sử dụng
- ✅ Resend verification hoạt động đúng
