# Tính năng Quên mật khẩu / Reset Password

## Tổng quan

Tính năng quên mật khẩu cho phép người dùng đặt lại mật khẩu thông qua email khi họ quên mật khẩu hiện tại.

## Các endpoint mới

### 1. POST /api/auth/forgot-password

**Mô tả:** Yêu cầu đặt lại mật khẩu thông qua email

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Nếu email của bạn tồn tại trong hệ thống, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu",
  "data": null
}
```

**Đặc điểm bảo mật:**
- Luôn trả về thành công để không tiết lộ thông tin về email có tồn tại hay không
- Rate limit: 3 requests per hour per IP
- Token reset có thời hạn 1 giờ
- Chỉ gửi email cho user có status ACTIVE

### 2. POST /api/auth/reset-password

**Mô tả:** Đặt lại mật khẩu với token từ email

**Request Body:**
```json
{
  "token": "your-reset-token-here",
  "newPassword": "NewPassword@123"
}
```

**Response thành công:**
```json
{
  "success": true,
  "message": "Mật khẩu đã được đặt lại thành công",
  "data": null
}
```

**Response lỗi:**
```json
{
  "success": false,
  "message": "Token không hợp lệ hoặc đã hết hạn",
  "data": null
}
```

**Validation mật khẩu:**
- Độ dài: 8-100 ký tự
- Phải chứa ít nhất:
  - 1 chữ thường (a-z)
  - 1 chữ hoa (A-Z)
  - 1 số (0-9)
  - 1 ký tự đặc biệt (@$!%*?&)

## Luồng hoạt động

1. **Yêu cầu reset password:**
   - User gửi email qua `/api/auth/forgot-password`
   - Hệ thống tạo token reset (UUID) với thời hạn 1 giờ
   - Lưu token vào `User.passwordResetToken` và `User.passwordResetExpires`
   - Gửi email chứa link reset password

2. **Đặt lại mật khẩu:**
   - User click link trong email hoặc gửi token qua `/api/auth/reset-password`
   - Hệ thống validate token và kiểm tra thời hạn
   - Mã hóa mật khẩu mới và cập nhật
   - Xóa token reset khỏi database

## Email Template

Email reset password có:
- Thiết kế responsive với HTML/CSS
- Thông tin bảo mật rõ ràng
- Link reset password với token
- Thông báo thời hạn 1 giờ
- Hướng dẫn nếu link không hoạt động

## Audit Logging

Tất cả hoạt động được ghi log qua `AuditLogger`:

- `PASSWORD_RESET_REQUEST_SUCCESS/FAILURE`: Yêu cầu reset password
- `PASSWORD_RESET_SUCCESS/FAILURE`: Thực hiện reset password thành công/thất bại
- `INVALID_PASSWORD_RESET_TOKEN`: Token không hợp lệ hoặc hết hạn

## Bảo mật

1. **Rate Limiting:** Giới hạn 3 requests/giờ cho forgot password
2. **Token Security:** Token UUID ngẫu nhiên với thời hạn 1 giờ
3. **Email Privacy:** Không tiết lộ thông tin về email có tồn tại
4. **Password Hashing:** Sử dụng BCrypt để mã hóa mật khẩu
5. **Status Check:** Chỉ cho phép reset cho user ACTIVE

## Database Schema

User entity đã có sẵn các trường:
```java
@Column(name = "password_reset_token")
private String passwordResetToken;

@Column(name = "password_reset_expires")
private LocalDateTime passwordResetExpires;
```

## Testing

Sử dụng Postman collection đã được cập nhật với các endpoint mới:

1. **Forgot Password Test:**
   ```bash
   POST {{baseUrl}}/api/auth/forgot-password
   Content-Type: application/json
   
   {
     "email": "test@example.com"
   }
   ```

2. **Reset Password Test:**
   ```bash
   POST {{baseUrl}}/api/auth/reset-password
   Content-Type: application/json
   
   {
     "token": "token-from-email",
     "newPassword": "NewPassword@123"
   }
   ```

## Lưu ý

- Cần cấu hình email server trong `application.properties`
- Frontend cần xử lý link reset password từ email
- Token sẽ tự động hết hạn sau 1 giờ
- Mỗi lần yêu cầu reset sẽ tạo token mới và vô hiệu hóa token cũ
