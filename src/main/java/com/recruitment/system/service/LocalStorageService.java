package com.recruitment.system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Locale;

@Service
@Slf4j
public class LocalStorageService implements StorageService {

    @Value("${app.upload.base-dir:uploads}")
    private String baseUploadDir;

    @Override
    public String save(MultipartFile file, String directory, String safeFileName) {
        try {
            String normalizedDir = directory.replace("\\", "/").replaceAll("/+", "/");
            if (normalizedDir.startsWith("/")) {
                normalizedDir = normalizedDir.substring(1);
            }

            Path root = Paths.get(baseUploadDir).toAbsolutePath().normalize();
            Path targetDir = root.resolve(normalizedDir).normalize();

            if (!targetDir.startsWith(root)) {
                throw new RuntimeException("Đường dẫn không hợp lệ");
            }

            Files.createDirectories(targetDir);

            String cleanedName = sanitizeFileName(safeFileName);
            Path targetFile = targetDir.resolve(cleanedName);

            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            Path relative = Paths.get(baseUploadDir).resolve(normalizedDir).resolve(cleanedName);
            String relativeStr = relative.toString().replace("\\", "/");
            log.info("Lưu file thành công: {}", targetFile);
            return "/" + relativeStr;
        } catch (IOException ex) {
            log.error("Lỗi khi lưu file", ex);
            throw new RuntimeException("Không thể lưu file");
        }
    }

    /**
     * Xóa file theo đường dẫn tương đối bắt đầu bằng "/uploads"
     */
    public boolean delete(String relativePath) {
        try {
            if (relativePath == null || relativePath.isBlank()) {
                return false;
            }
            String normalized = relativePath.replace("\\", "/");
            if (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            Path root = Paths.get(baseUploadDir).toAbsolutePath().normalize();
            Path target = Paths.get(normalized).toAbsolutePath().normalize();
            if (!target.startsWith(root)) {
                // Nếu path không chứa base dir, cố gắng ghép
                target = root.resolve(normalized).normalize();
            }
            if (!target.startsWith(root)) {
                throw new RuntimeException("Đường dẫn không hợp lệ");
            }
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Không thể xóa file: {}", relativePath, e);
            return false;
        }
    }

    private String sanitizeFileName(String original) {
        String filename = StringUtils.hasText(original) ? original : "file";
        filename = Normalizer.normalize(filename, Normalizer.Form.NFD)
                .replaceAll("[^\u0000-\u007F]", "");
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "-");
        if (!filename.contains(".")) {
            filename = filename + ".pdf";
        }
        if (filename.length() > 200) {
            String ext = filename.substring(filename.lastIndexOf('.'));
            filename = filename.substring(0, 200 - ext.length()) + ext;
        }
        return filename.toLowerCase(Locale.ROOT);
    }
}


