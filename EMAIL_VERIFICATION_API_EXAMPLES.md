# Email Verification API Examples

## 1. Đăng ký tài khoản mới

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "APPLICANT"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "accessTokenExpiration": 900000,
    "refreshTokenExpiration": 2592000000,
    "user": {
      "id": 16,
      "email": "newuser@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "fullName": "John Doe",
      "phoneNumber": null,
      "role": "APPLICANT",
      "status": "ACTIVE",
      "emailVerified": false,
      "avatarUrl": null,
      "lastLogin": null,
      "createdAt": "2025-01-15T10:30:00"
    }
  }
}
```

**Lưu ý:** `emailVerified` sẽ là `false` và email xác minh sẽ được gửi tự động.

## 2. Thử đăng nhập trước khi xác minh email

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123"
  }'
```

**Response (Lỗi):**
```json
{
  "success": false,
  "message": "Vui lòng xác minh email trước khi đăng nhập. Kiểm tra hộp thư của bạn hoặc yêu cầu gửi lại email xác minh."
}
```

## 3. Xác minh email

### Cách 1: Nhấp vào link trong email
Email sẽ chứa link: `http://localhost:3000/verify-email?token=uuid-verification-token`

### Cách 2: Gửi POST request trực tiếp
```bash
curl -X POST http://localhost:8081/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "token": "uuid-verification-token-from-email"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Email đã được xác minh thành công",
  "data": null
}
```

## 4. Đăng nhập sau khi xác minh email

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "accessTokenExpiration": 900000,
    "refreshTokenExpiration": 2592000000,
    "user": {
      "id": 16,
      "email": "newuser@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "fullName": "John Doe",
      "phoneNumber": null,
      "role": "APPLICANT",
      "status": "ACTIVE",
      "emailVerified": true,
      "avatarUrl": null,
      "lastLogin": "2025-01-15T10:35:00",
      "createdAt": "2025-01-15T10:30:00"
    }
  }
}
```

## 5. Gửi lại email xác minh

```bash
curl -X POST http://localhost:8081/api/auth/resend-verification \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Email xác minh đã được gửi lại",
  "data": null
}
```

## 6. Kiểm tra thông tin user hiện tại

```bash
curl -X GET http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Thông tin người dùng",
  "data": {
    "id": 16,
    "email": "newuser@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "fullName": "John Doe",
    "phoneNumber": null,
    "role": "APPLICANT",
    "status": "ACTIVE",
    "emailVerified": true,
    "avatarUrl": null,
    "lastLogin": "2025-01-15T10:35:00",
    "createdAt": "2025-01-15T10:30:00"
  }
}
```

## Error Cases

### 1. Token không hợp lệ
```json
{
  "success": false,
  "message": "Token xác minh không hợp lệ hoặc đã hết hạn"
}
```

### 2. Token đã hết hạn (24 giờ)
```json
{
  "success": false,
  "message": "Token xác minh đã hết hạn. Vui lòng yêu cầu gửi lại email xác minh."
}
```

### 3. Email đã được xác minh
```json
{
  "success": false,
  "message": "Email này đã được xác minh"
}
```

### 4. Không tìm thấy tài khoản
```json
{
  "success": false,
  "message": "Không tìm thấy tài khoản với email này"
}
```

### 5. Rate limit exceeded
```json
{
  "success": false,
  "message": "Quá nhiều yêu cầu gửi lại email. Vui lòng thử lại sau 3600 giây."
}
```

## Testing với Postman

### Collection Import
Bạn có thể import file `Recruitment System API.postman_collection.json` để test các endpoints.

### Environment Variables
Tạo environment variables:
- `baseUrl`: `http://localhost:8081`
- `accessToken`: Token từ response login
- `verificationToken`: Token từ email hoặc database

### Test Flow
1. Register new user
2. Try to login (should fail)
3. Check email or database for verification token
4. Verify email with token
5. Login successfully
6. Get user info to confirm emailVerified = true

## Database Queries

### Kiểm tra token verification
```sql
SELECT id, email, email_verified, verification_token, created_at 
FROM users 
WHERE email = 'newuser@example.com';
```

### Xem tất cả users chưa xác minh email
```sql
SELECT id, email, email_verified, verification_token, created_at 
FROM users 
WHERE email_verified = 0;
```

### Xóa token hết hạn (cleanup)
```sql
UPDATE users 
SET verification_token = NULL 
WHERE email_verified = 0 
AND created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR);
```
