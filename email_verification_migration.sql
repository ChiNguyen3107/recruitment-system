-- Migration script để cập nhật email verification cho dữ liệu hiện tại
-- Chạy script này nếu bạn đã có dữ liệu trong database

-- Cập nhật tất cả users hiện tại để email_verified = false (trừ admin)
-- Admin và các user test có thể giữ email_verified = true
UPDATE users 
SET email_verified = 0, verification_token = NULL 
WHERE email NOT IN ('admin@recruitment.com', 'employer@techinnovate.com');

-- Hoặc nếu bạn muốn tất cả users cần xác minh lại email:
-- UPDATE users SET email_verified = 0, verification_token = NULL;

-- Tạo index để tối ưu hóa tìm kiếm theo verification_token
CREATE INDEX idx_users_verification_token ON users(verification_token);

-- Kiểm tra kết quả
SELECT id, email, email_verified, verification_token, created_at 
FROM users 
ORDER BY created_at DESC;
