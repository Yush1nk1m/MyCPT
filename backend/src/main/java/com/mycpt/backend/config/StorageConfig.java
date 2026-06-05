package com.mycpt.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 로컬 스토리지 정적 리소스 서빙 설정 (개발 환경)
 * /images/** 요청을 로컬 업로드 디렉터리로 매핑
 * 운영 환경(S3)에서는 이 설정이 사용되지 않음
 */
@Configuration
public class StorageConfig implements WebMvcConfigurer {

    @Value("${storage.local.base-path:./uploads}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + basePath + "/");
    }
}
