package com.mycpt.backend.common.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 로컬 파일 시스템 스토리지 (개발 환경)
 * @Profile("!prod") - 운영 환경에서는 S3StorageService로 교체
 *
 * 저장 경로: ${storage.local.base-path}/{prefix}/{uuid}.{ext}
 * 반환 URL:  ${storage.local.base-url}/{prefix}/{uuid}.{ext}
 */
@Service
@Profile("!prod")
public class LocalStorageService implements StorageService {

    private final String basePath;
    private final String baseUrl;

    public LocalStorageService(
            @Value("${storage.local.base-path:./uploads}") String basePath,
            @Value("${storage.local.base-url:http://localhost:8080/images}") String baseUrl
    ) {
        this.basePath = basePath;
        this.baseUrl = baseUrl;
    }

    @Override
    public String store(MultipartFile file, String prefix) {
        String ext = extractExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + ext;
        Path dir = Paths.get(basePath, prefix);

        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }

        // Full URL 반환 - maintenance-guide.md: 항상 Full URL 저장
        return baseUrl + "/" + prefix + "/" + filename;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
