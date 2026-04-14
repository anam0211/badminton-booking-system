package com.badminton.booking.common.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageStorageService {

    @Value("${app.upload.dir:src/main/resources/static/uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            log.error("Could not create upload directory: {}", uploadPath, e);
        }
    }

    /**
     * Lưu file ảnh vào thư mục con (subDir) và trả về tên file duy nhất.
     *
     * @param file   Multipart file
     * @param subDir Thư mục con (ví dụ: "branches", "reviews")
     * @return Tên file duy nhất (bao gồm subDir, ví dụ: "branches/abc.jpg")
     */
    public String storeImage(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File ảnh không được để trống.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh.");
        }

        String originalName = file.getOriginalFilename();
        String extension = getFileExtension(originalName);
        String fileName = UUID.randomUUID() + extension;

        try {
            Path subDirPath = uploadPath.resolve(subDir);
            Files.createDirectories(subDirPath);
            Path target = subDirPath.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return subDir + "/" + fileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể lưu file ảnh: " + ex.getMessage(), ex);
        }
    }

    /**
     * Xóa file ảnh khỏi thư mục.
     *
     * @param relativePath Đường dẫn tương đối (ví dụ: "branches/abc.jpg")
     */
    public void deleteImage(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return;
        }
        try {
            Path filePath = uploadPath.resolve(relativePath).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete image file: {}", relativePath, e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return ".jpg";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return ".jpg";
        }
        String extension = filename.substring(dotIndex).toLowerCase(Locale.ROOT);
        if (extension.length() > 10) {
            return ".jpg";
        }
        return extension;
    }
}
