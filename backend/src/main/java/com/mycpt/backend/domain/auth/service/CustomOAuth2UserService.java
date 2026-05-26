package com.mycpt.backend.domain.auth.service;

import com.mycpt.backend.domain.auth.dto.KakaoUserInfo;
import com.mycpt.backend.domain.auth.dto.UserPrincipal;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

// DefaultOAuth2UserService가 카카오 userinfo 엔드포인트 HTTP 호출, 응답 JSON 파싱, OAuth2User 객체 생성까지 처리
// loadUser() 메서드만 오버라이드하여 결과를 DB에 저장하는 처리 로직만 추가하면 됨
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    // 신규 회원 저장과 기존 회원 조회를 동일 트랜잭션 내에서 처리하여 중복 가입 방지
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. 부모 클래스가 카카오 userinfo API를 호출하고 응답을 OAuth2User로 변환
        //    내부적으로 application.yml의 user-info-uri, user-name-attribute를 참조
        //    네트워크 오류나 토큰 만료 시 OAuth2AuthenticationException을 던짐
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 카카오 응답 Map 객체를 KakaoUserInfo DTO 객체로 래핑하여 필드 접근을 명확히
        //    oAuth2User.getAttributes() 메서드는 카카오 JSON 응답 전체를 Map으로 담고 있음
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());

        // 3. kakao_id로 기존 회원 조회
        //      - 존재: orElseGet() 람다 실행하지 않고 기존 User 엔티티 반환
        //      - 부재: orElseGet() 람다 실행 -> User.create()로 엔티티 생성 -> DB INSERT
        User user = findOrCreateUser(kakaoUserInfo);

        // 4. UserPrincipal 객체로 래핑하여 반환
        //    Spring Security는 이 반환 값을 Authentication 객체에 담아 SecurityContext -> 세션(Redis)에 저장
        //    이후 사용자 요청마다 세션에서 복원되어 @AuthenticationPrincipal로 주입
        return new UserPrincipal(user, oAuth2User.getAttributes());
    }

    @Transactional
    User findOrCreateUser(KakaoUserInfo kakaoUserInfo) {
        // TODO: 신규 가입 시 코인 3개 초기 지급 결과를 coin_transactions 테이블에 INSERT 필요.
        //       CoinService 구현 후 save() 블록 안에 추가
        return userRepository.findByKakaoId(kakaoUserInfo.getId())
                .orElseGet(() -> userRepository.save(
                        User.create(
                                kakaoUserInfo.getId(),
                                kakaoUserInfo.getNickname(),
                                kakaoUserInfo.getProfileImageUrl())));
    }
}
