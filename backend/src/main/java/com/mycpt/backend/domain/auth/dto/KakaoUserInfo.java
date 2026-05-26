package com.mycpt.backend.domain.auth.dto;

import java.util.Map;

// 카카오 userinfo API 응답 JSON을 파싱하는 DTO
// CustomOAuth2UserService에서 OAuth2User.getAttributes() 메서드에 의해 반환된 Map을 래핑하여 사용
//
// 카카오 응답 JSON 구조 (참고):
// {
//   "id": 1234567890,                     ← 카카오 고유 사용자 식별자
//   "properties": {
//     "nickname": "김유신",               ← 카카오 프로필 닉네임
//     "profile_image": "https://..."      ← 프로필 이미지 원본 URL
//   }
// }
public class KakaoUserInfo {

    // OAuth2User.getAttributes() 메서드가 반환한 최상위 Map
    // 키: "id", "properties" 등 카카오 응답의 최상위 필드명
    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // 카카오 사용자 고유 식별자
    // VARCHAR(50) 타입에 맞게 Long -> String 타입 변환 필요
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    // 카카오 프로필 닉네임
    @SuppressWarnings("unchecked")  // 카카오 응답 스펙이 고정적이므로 Map<String, Object> 캐스팅 시 컴파일러 unchecked 경고 억제
    public String getNickname() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) return "닉네임";
        return (String) properties.getOrDefault("nickname", "닉네임");
    }

    // 카카오 프로필 이미지 원본 URL
    // 신규 가입 시 users.profile_image_key 필드 초기 값으로 사용 -> 사용자가 프로필 이미지 업로드 시 S3 키로 교체
    // properties가 null이거나 이미지가 없으면 null 반환 -> 프론트에서 기본 이미지 사용
    @SuppressWarnings("unchecked")
    public String getProfileImageUrl() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) return null;
        return (String) properties.get("profile_image");
    }
}
