package com.mycpt.backend.domain.auth.dto;

import com.mycpt.backend.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

// Spring Security가 인증 완료 후 SecurityContext에 저장하는 Principal 객체
// OAuth2User 인터페이스를 구현해야 Spring Security가 인증 객체로 인식
//
// 역할:
// - SecurityContext에서 현재 로그인한 User 엔티티를 꺼낼 수 있는 래퍼
// - 컨트롤러에서 @AuthenticationPrincipal UserPrincipal principal 로 주입받아 principal.getUser() 메서드로 User 엔티티에 즉시 접근
//
// OAuth2User를 직접 쓰지 않는 이유
// - 기본 OAuth2User는 attributes Map만 갖고 있어서 User 엔티티는 매번 DB 조회를 통해 꺼내야 함
// - UserPrincipal이 User 엔티티를 직접 들고 있으면 컨트롤러에서 추가 조회 없이 사용 가능
@Getter
public class UserPrincipal implements OAuth2User {

    // 인증된 사용자의 User 엔티티
    // CustomOAuth2UserService.loadUser()에서 DB 조회/저장 후 주입됨
    private final User user;

    // 카카오 userinfo 응답 원본 Map
    // OAuth2User 인터페이스 구현 요구사항이며, 필요 시 원본 카카오 데이터 접근 가능
    private final Map<String, Object> attributes;

    public UserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // OAuth2User 인터페이스 구현 메서드 (1/3)
    // Principal의 고유 이름. Spring Security 내부적으로 사용자 식별 시 사용
    // kakao_id 대신 users.id 사용하여 외부 식별자 노출 최소화
    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }

    // OAuth2User 인터페이스 구현 메서드 (2/3)
    // 사용자의 권한(역할) 목록. 현재는 회원/비회원만 구분하므로 빈 리스트 반환
    // TODO: 추후 관리자 역할 등 추가 시 이 메서드에 ROLE_ADMIN 등을 추가
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    // OAuth2User 인터페이스 구현 메서드 (3/3)
    // 카카오 userinfo 응답 원본 Map 반환
    // @Getter 애너테이션에 의해 getAttributes() 메서드 생성. Lombok이 구현 메서드로 인식

    // 정적 팩토리 메서드
    public static UserPrincipal from(User user) {
        return new UserPrincipal(user, Collections.emptyMap());
    }
}
