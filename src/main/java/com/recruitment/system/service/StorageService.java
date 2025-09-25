package com.recruitment.system.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface trừu tượng cho lớp lưu trữ file (có thể thay bằng S3 trong tương lai)
 */
public interface StorageService {

    /**
     * Lưu file và trả về đường dẫn/URL có thể truy cập (public hoặc relative)
     *
     * @param file Multipart file upload
     * @param directory thư mục tương đối để lưu, ví dụ: "uploads/resumes/123"
     * @param safeFileName tên file an toàn đã chuẩn hóa
     * @return resumeUrl (public hoặc relative) để client sử dụng
     */
    String save(MultipartFile file, String directory, String safeFileName);
}


