package com.mycpt.backend.domain.user.entity;

import com.mycpt.backend.domain.user.enums.Gender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
// protected 기본 생성자 정의하여 create() 정적 팩토리 메서드를 통해서만 객체 생성 가능
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    // 고유 식별자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카카오가 발급하는 사용자 고유 식별자(숫자이지만 VARCHAR 타입으로 관리)
    @Column(nullable = false, unique = true, length = 50)
    private String kakaoId;

    // 사용자 닉네임
    @Column(nullable = false, length = 30)
    private String nickname;

    // 스토리지 오브젝트 키 또는 외부 이미지 URL
    // 신규 가입 시 카카오 프로필 이미지 URL을 그대로 저장
    // 프로필 이미지 업로드 후 S3 오브젝트 키를 조합한 Full URL로 교체
    // null이면 프론트엔드에서 기본 이미지 사용
    @Column(length = 300)
    private String profileImageUrl;

    // 사용자 생년
    // YEAR 타입과 매핑되지만 Java에서는 Integer 타입으로 관리
    // 로그인 후 프로필 변경 전까지 null로 설정
    @Column(columnDefinition = "YEAR")
    private Integer birthYear;

    // 사용자 성별(M/F/N)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    // 사용자 코인 잔액
    @Column(nullable = false, columnDefinition = "TINYINT")
    private int coins;

    // 다음 코인 충전 예정 시각(null일 시 코인 망충)
    private LocalDateTime nextCoinAt;

    // 사용자 가입 시각
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 정적 팩토리 메서드
     *
     * 1. 메서드 이름을 통해 의도 표현
     * 2. 생성 시 반드시 설정해야 하는 필드(coins, createdAt) 누락 방지
     */
    public static User create(String kakaoId, String nickname, String profileImageUrl) {
        User user = new User();
        user.kakaoId = kakaoId;
        user.nickname = nickname;
        user.profileImageUrl = profileImageUrl;
        user.coins = 3; // 신규 가입 시 초기 코인은 3으로 설정
        user.createdAt = LocalDateTime.now();
        return user;
    }

    /**
     * 프로필 정보 수정 (PATCH /users/me)
     * null 필드는 기존 값 유지 - 요청에 포함된 필드만 업데이트
     */
    public void updateProfile(String nickname, Integer birthYear, Gender gender) {
        if (nickname != null) this.nickname = nickname;
        if (birthYear != null) this.birthYear = birthYear;
        if (gender != null) this.gender = gender;
    }

    /**
     * 프로필 이미지 URL 교체 (POST /users/me/profile-image)
     * 스토리지 저장 후 반환된 Full URL을 그대로 저장 (maintenance-guide.md 정책)
     */
    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
