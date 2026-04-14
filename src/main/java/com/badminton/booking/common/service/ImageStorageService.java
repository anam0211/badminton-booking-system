package com.badminton.booking.common.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
     * Lưu file ảnh vào thư mục con và trả về đường dẫn lưu tương đối.
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
     * Xóa file ảnh local nếu tồn tại. Remote URL sẽ được bỏ qua.
     */
    public void deleteImage(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty() || isRemoteUrl(relativePath)) {
            return;
        }
        try {
            Path filePath = uploadPath.resolve(relativePath).normalize();
            Files.deleteIfExists(filePath);
        } catch (InvalidPathException e) {
            log.warn("Skip deleting invalid image path: {}", relativePath);
        } catch (IOException e) {
            log.warn("Could not delete image file: {}", relativePath, e);
        }
    }

    private boolean isRemoteUrl(String path) {
        String normalized = path.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("http://") || normalized.startsWith("https://");
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
