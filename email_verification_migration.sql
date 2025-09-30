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

-- Thêm cột thời điểm phát hành token (nullable để backward-compatible)
ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_token_issued_at TIMESTAMP NULL DEFAULT NULL;

-- Gán giá trị phát hành cho các user hiện có có token (dùng created_at làm mặc định)
UPDATE users SET verification_token_issued_at = created_at WHERE verification_token IS NOT NULL AND verification_token_issued_at IS NULL;

-- Kiểm tra kết quả
SELECT id, email, email_verified, verification_token, created_at 
FROM users 
ORDER BY created_at DESC;
