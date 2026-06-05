package com.mycpt.backend.common.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 스토리지 추상화 인터페이스
 * 개발: LocalStorageService (로컬 파일 시스템)
 * 운영: S3StorageService (AWS S3) - 4주차 교체
 */
public interface StorageService {
    /**
     * 파일을 저장하고 접근 가능한 Full URL을 반환
     * @param file      업로드할 파일
     * @param prefix    저장 경로 접두어 (예: "profiles")
     * @return          Full URL (예: "http://localhost:8080/images/profiles/image.jpg")
     */
    String store(MultipartFile file, String prefix);
}
