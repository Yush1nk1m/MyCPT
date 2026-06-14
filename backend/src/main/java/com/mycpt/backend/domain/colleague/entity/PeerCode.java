package com.mycpt.backend.domain.colleague.entity;

import com.mycpt.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 동료 초대 코드 (peer_codes 테이블)
 *
 * 설계:
 *  - 사용자당 1행 (UNIQUE user_id) - 코드가 없으면 INSERT, 있으면 UPDATE
 *  - 대문자 + 숫자 8자리 랜덤 코드 (CHAR(8))
 *  - 만료: 발급 시점 +7일. 배치 삭제 기준
 *  - 온디맨드 리프레시: GET /peer-code 조회 시 만료됐으면 자동 갱신
 *                    POST /peer-code/refresh 호출 시 강제 갱신
 */
@Entity
@Table(name = "peer_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PeerCode {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자당 1행 보장 (UNIQUE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, columnDefinition = "CHAR(8)")
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 신규 생성 팩토리
     */
    public static PeerCode create(User user, long ttlDays) {
        PeerCode pc = new PeerCode();
        pc.user = user;
        pc.createdAt = LocalDateTime.now();
        pc.code = generateCode();
        pc.expiresAt = pc.createdAt.plusDays(ttlDays);
        return pc;
    }

    /**
     * 코드 갱신 (온디맨드 리프레시 / 강제 리프레시 공통)
     * dirty checking으로 UPDATE - 별도 save() 불필요하나 명시적 save()는 서비스에서 수행
     */
    public void refresh(long ttlDays) {
        this.code = generateCode();
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusDays(ttlDays);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; ++i) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
