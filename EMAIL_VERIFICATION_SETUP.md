# Hướng dẫn cấu hình Email Verification

## Tổng quan

Hệ thống đã được tích hợp tính năng xác minh email với các endpoint sau:

- `POST /api/auth/verify-email` - Xác minh email với token
- `POST /api/auth/resend-verification` - Gửi lại email xác minh

## Cấu hình Email

### 1. Cấu hình Gmail SMTP

Để sử dụng Gmail SMTP, bạn cần:

1. **Tạo App Password cho Gmail:**
   - Đăng nhập vào Gmail
   - Vào Settings > Security
   - Bật 2-Step Verification
   - Tạo App Password cho ứng dụng

2. **Cập nhật application.properties:**

```properties
# Gmail SMTP Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-character-app-password
```

### 2. Cấu hình Environment Variables

Thay vì hardcode trong file properties, bạn có thể sử dụng environment variables:

```bash
# Windows
set MAIL_USERNAME=your-email@gmail.com
set MAIL_PASSWORD=your-app-password

# Linux/Mac
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

### 3. Cấu hình Frontend URL

Cập nhật URL frontend để email verification links hoạt động đúng:

```properties
app.frontend.url=http://localhost:3000
```

## Luồng hoạt động

### 1. Đăng ký tài khoản

```json
POST /api/auth/register
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "APPLICANT"
}
```

**Response:** Tài khoản được tạo, `emailVerified = false`, email xác minh được gửi tự động.

### 2. Xác minh email

Người dùng nhấp vào link trong email hoặc gửi POST request:

```json
POST /api/auth/verify-email
{
  "token": "uuid-verification-token"
}
```

**Response:** `emailVerified = true`, token bị xóa.

### 3. Gửi lại email xác minh

```json
POST /api/auth/resend-verification
{
  "email": "user@example.com"
}
```

**Response:** Token mới được tạo và email được gửi lại.

### 4. Đăng nhập

```json
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:** 
- Nếu `emailVerified = true`: Đăng nhập thành công
- Nếu `emailVerified = false`: Lỗi yêu cầu xác minh email

## Cấu trúc Database

### User Entity

```sql
ALTER TABLE users 
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN verification_token VARCHAR(255);
```

## Email Template

Email được tạo từ template Thymeleaf tại `src/main/resources/templates/verification-email.html`.

Template hỗ trợ các biến:
- `${userName}` - Tên đầy đủ người dùng
- `${verificationLink}` - Link xác minh
- `${expirationHours}` - Số giờ hết hạn (mặc định 24h)
- `${appName}` - Tên ứng dụng
- `${currentYear}` - Năm hiện tại

## Bảo mật

### Rate Limiting

- **Resend verification:** 3 requests per hour per IP
- **Verify email:** Không giới hạn (nhưng token chỉ dùng được 1 lần)

### Token Security

- Token là UUID ngẫu nhiên
- Hết hạn sau 24 giờ
- Tự động xóa sau khi sử dụng
- Không thể tái sử dụng

### Audit Logging

Tất cả hoạt động email verification được ghi log:
- `EMAIL_VERIFICATION_SUCCESS/FAILURE`
- `RESEND_VERIFICATION_SUCCESS/FAILURE`

## Testing

### 1. Test với MailHog (Development)

Cài đặt MailHog để test email locally:

```bash
# Download và chạy MailHog
# Truy cập http://localhost:8025 để xem emails
```

Cấu hình application.properties:

```properties
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.username=
spring.mail.password=
```

### 2. Test với Gmail

Sử dụng Gmail SMTP với App Password như hướng dẫn trên.

## Troubleshooting

### Lỗi thường gặp

1. **"Authentication failed"**
   - Kiểm tra App Password đúng chưa
   - Đảm bảo 2-Step Verification đã bật

2. **"Connection timeout"**
   - Kiểm tra firewall/network
   - Thử port 465 với SSL thay vì 587 với STARTTLS

3. **"Email không được gửi"**
   - Kiểm tra log để xem lỗi cụ thể
   - Verify cấu hình SMTP

### Debug Mode

Bật debug logging để xem chi tiết:

```properties
logging.level.org.springframework.mail=DEBUG
logging.level.com.recruitment.system.service.MailService=DEBUG
```

## Production Considerations

1. **Sử dụng Email Service chuyên nghiệp:**
   - SendGrid
   - AWS SES
   - Mailgun

2. **Cấu hình DNS:**
   - SPF record
   - DKIM signature
   - DMARC policy

3. **Monitoring:**
   - Email delivery rates
   - Bounce rates
   - Spam complaints

4. **Backup email service:**
   - Chuẩn bị service dự phòng
   - Failover mechanism
