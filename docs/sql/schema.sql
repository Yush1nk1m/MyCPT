-- ============================================================
-- MyCPT 데이터베이스 스키마
-- Version : v0.4
-- Based on : database-design.md v0.4 / service-design.md v0.6
--
-- 생성 순서 (FK 의존성 기준)
--   1. users
--   2. disc_cache             ← test_results 의 복합 FK 타깃
--                                보고서 Markdown 단일 TEXT로 통합
--   3. test_results           ← rater_type, label 컬럼 추가
--   4. coin_transactions
--   5. peer_codes
--   6. assessment_tokens      ← 타인 평정 일회용 링크 토큰
--   7. colleagues
--   8. chemistry_reports      ← 보고서 Markdown 단일 TEXT로 통합
--   9. notifications
--  (statistics 테이블 제거 — MVP에서 직접 집계 쿼리로 대체)
-- ============================================================

-- 기존 테이블 제거 (개발 환경 재초기화용, 운영 시 제거)
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS chemistry_reports;
DROP TABLE IF EXISTS colleagues;
DROP TABLE IF EXISTS assessment_tokens;
DROP TABLE IF EXISTS peer_codes;
DROP TABLE IF EXISTS coin_transactions;
DROP TABLE IF EXISTS test_results;
DROP TABLE IF EXISTS disc_cache;
DROP TABLE IF EXISTS users;

-- ============================================================
-- 1. users
-- ============================================================
CREATE TABLE users (
    id                  BIGINT          NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    kakao_id            VARCHAR(50)     NOT NULL                  COMMENT '카카오 고유 식별자',
    nickname            VARCHAR(30)     NOT NULL                  COMMENT '서비스 닉네임 (카카오 닉네임 초기값, 수정 가능)',
    profile_image_key   VARCHAR(300)    NULL                      COMMENT '스토리지 오브젝트 키. NULL 이면 기본 이미지 사용',
    birth_year          YEAR            NULL                      COMMENT '카카오 로그인 후 프로필 설정 시 입력. NULL 이면 미입력 상태',
    gender              ENUM('M','F','N') NULL                    COMMENT 'M: 남성 / F: 여성 / N: 선택 안 함',
    coins               TINYINT         NOT NULL DEFAULT 3        COMMENT '현재 코인 잔액 (0~3)',
    next_coin_at        DATETIME        NULL                      COMMENT '다음 코인 충전 예정 시각. NULL 이면 만충 상태',
    created_at          DATETIME        NOT NULL                  COMMENT '가입 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_kakao_id (kakao_id)
) COMMENT = '회원 정보. 카카오 OAuth 기반 가입';

-- ============================================================
-- 2. disc_cache
--    복합 PK (d, i, s, c) — 행 DELETE 없이 UPDATE 로만 갱신
--    test_results 가 복합 FK 로 참조하므로 먼저 생성
--    보고서를 Markdown 단일 TEXT로 저장 — 검사 유형 확장 시 스키마 변경 없음
-- ============================================================
CREATE TABLE disc_cache (
    d           TINYINT     NOT NULL    COMMENT 'D 버킷값 (1~9)',
    i           TINYINT     NOT NULL    COMMENT 'I 버킷값 (1~9)',
    s           TINYINT     NOT NULL    COMMENT 'S 버킷값 (1~9)',
    c           TINYINT     NOT NULL    COMMENT 'C 버킷값 (1~9)',
    report      TEXT        NOT NULL    COMMENT 'Markdown 형식 분석 보고서 전문. 이름 미포함. 렌더링 시 이름 삽입',
    created_at  DATETIME    NOT NULL    COMMENT '캐시 생성 시각 (온디맨드 만료 판단 기준)',

    PRIMARY KEY (d, i, s, c)
) COMMENT = 'DISC 버킷 기반 보고서 캐시. 최대 9^4 = 6,561 행. 행 삭제 없이 UPDATE 갱신. Markdown 단일 TEXT';

-- ============================================================
-- 3. test_results
-- ============================================================
CREATE TABLE test_results (
    id          BIGINT      NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    user_id     BIGINT      NOT NULL                  COMMENT 'FK → users.id. 결과 귀속 대상 (피평정자)',
    rater_type  ENUM('SELF','OTHER') NOT NULL DEFAULT 'SELF'
                                                      COMMENT 'SELF: 자기 평정 / OTHER: 타인 평정',
    label       VARCHAR(30) NULL                      COMMENT '타인 평정 시 식별 라벨 (예: 여자친구). 자기 평정은 NULL',
    d_score     TINYINT     NOT NULL                  COMMENT 'D 원점수 (-24 ~ +48)',
    i_score     TINYINT     NOT NULL                  COMMENT 'I 원점수 (-24 ~ +48)',
    s_score     TINYINT     NOT NULL                  COMMENT 'S 원점수 (-24 ~ +48)',
    c_score     TINYINT     NOT NULL                  COMMENT 'C 원점수 (-24 ~ +48)',
    d_bucket    TINYINT     NOT NULL                  COMMENT 'D 버킷값 (1~9). disc_cache 복합 FK',
    i_bucket    TINYINT     NOT NULL                  COMMENT 'I 버킷값 (1~9)',
    s_bucket    TINYINT     NOT NULL                  COMMENT 'S 버킷값 (1~9)',
    c_bucket    TINYINT     NOT NULL                  COMMENT 'C 버킷값 (1~9)',
    created_at  DATETIME    NOT NULL                  COMMENT '검사 완료 시각',

    PRIMARY KEY (id),
    KEY idx_test_results_user_id (user_id),
    KEY idx_test_results_rater_type (rater_type),

    CONSTRAINT fk_test_results_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),

    CONSTRAINT fk_test_results_disc_cache
        FOREIGN KEY (d_bucket, i_bucket, s_bucket, c_bucket)
        REFERENCES disc_cache (d, i, s, c)
) COMMENT = '회원 검사 결과 이력. 자기/타인 평정 구분. 비회원은 클라이언트 sessionStorage 보관 후 로그인 시 원점수 전송';

-- ============================================================
-- 4. coin_transactions
-- ============================================================
CREATE TABLE coin_transactions (
    id              BIGINT      NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    user_id         BIGINT      NOT NULL                  COMMENT 'FK → users.id',
    amount          TINYINT     NOT NULL                  COMMENT '양수: 충전 / 음수: 차감',
    reason          ENUM('SIGNUP', 'RECHARGE', 'CHEMISTRY_REPORT') NOT NULL
                                                          COMMENT 'SIGNUP: 가입 지급 / RECHARGE: 주기 충전 / CHEMISTRY_REPORT: 보고서 차감',
    balance_after   TINYINT     NOT NULL                  COMMENT '트랜잭션 후 잔액 (이상 감지 용도)',
    created_at      DATETIME    NOT NULL                  COMMENT '트랜잭션 발생 시각',

    PRIMARY KEY (id),
    KEY idx_coin_transactions_user_id (user_id),

    CONSTRAINT fk_coin_transactions_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
) COMMENT = '코인 충전/차감 이력. 이상 감지 및 CS 대응 용도';

-- ============================================================
-- 5. peer_codes
-- ============================================================
CREATE TABLE peer_codes (
    id          BIGINT      NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    user_id     BIGINT      NOT NULL                  COMMENT 'FK → users.id. 사용자당 1행',
    code        CHAR(8)     NOT NULL                  COMMENT '대문자+숫자 8자리 랜덤 코드',
    expires_at  DATETIME    NOT NULL                  COMMENT '만료 시각 (발급 시점 +7일). 배치 삭제 기준',
    created_at  DATETIME    NOT NULL                  COMMENT '발급 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uq_peer_codes_user_id (user_id),
    UNIQUE KEY uq_peer_codes_code    (code),
    KEY idx_peer_codes_expires_at    (expires_at),

    CONSTRAINT fk_peer_codes_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
) COMMENT = '동료 초대 코드. 사용자당 1행, 7일 만료, 온디맨드 리프레시';

-- ============================================================
-- 6. assessment_tokens
--    "나는 어떤 사람인가요?" 타인 평정 일회용 링크 토큰
--    사용 완료(used=true) 후 재접속 차단으로 중복 제출 방지
--    만료 토큰 삭제: peer_codes 배치와 통합하여 매일 새벽 실행
-- ============================================================
CREATE TABLE assessment_tokens (
    id          BIGINT      NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    subject_id  BIGINT      NOT NULL                  COMMENT 'FK → users.id. 평정 대상자 (링크 생성한 회원)',
    token       CHAR(32)    NOT NULL                  COMMENT '일회용 랜덤 토큰',
    label       VARCHAR(30) NULL                      COMMENT '평정자 식별 라벨 (예: 여자친구). 결과 저장 시 test_results.label 에 복사',
    used        BOOLEAN     NOT NULL DEFAULT FALSE    COMMENT '사용 여부. TRUE 이면 재접속 차단',
    expires_at  DATETIME    NOT NULL                  COMMENT '만료 시각 (생성 시점 +7일)',
    created_at  DATETIME    NOT NULL                  COMMENT '생성 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uq_assessment_tokens_token (token),
    KEY idx_assessment_tokens_subject_id  (subject_id),
    KEY idx_assessment_tokens_expires_at  (expires_at),

    CONSTRAINT fk_assessment_tokens_subject
        FOREIGN KEY (subject_id)
        REFERENCES users (id)
) COMMENT = '타인 평정 일회용 링크 토큰. 7일 만료. used=TRUE 시 재제출 차단';

-- ============================================================
-- 7. colleagues
--    user_a_id < user_b_id 저장 규칙 → UNIQUE (user_a_id, user_b_id) 만으로 중복 방지
-- ============================================================
CREATE TABLE colleagues (
    id          BIGINT      NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    user_a_id   BIGINT      NOT NULL                  COMMENT 'FK → users.id. 두 사용자 중 작은 ID',
    user_b_id   BIGINT      NOT NULL                  COMMENT 'FK → users.id. 두 사용자 중 큰 ID',
    created_at  DATETIME    NOT NULL                  COMMENT '동료 관계 생성 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uq_colleagues_pair      (user_a_id, user_b_id),
    KEY idx_colleagues_user_a_id       (user_a_id),
    KEY idx_colleagues_user_b_id       (user_b_id),

    CONSTRAINT fk_colleagues_user_a
        FOREIGN KEY (user_a_id)
        REFERENCES users (id),

    CONSTRAINT fk_colleagues_user_b
        FOREIGN KEY (user_b_id)
        REFERENCES users (id)
) COMMENT = '동료 관계. user_a_id < user_b_id 규칙으로 단일 행에 양방향 관계 저장';

-- 양방향 동료 목록 조회
-- SELECT user_b_id AS colleague_id FROM colleagues WHERE user_a_id = :userId
-- UNION ALL
-- SELECT user_a_id AS colleague_id FROM colleagues WHERE user_b_id = :userId

-- ============================================================
-- 8. chemistry_reports
--    보고서를 Markdown 단일 TEXT로 저장
--    검사 유형(DISC/MBTI/BIG5 등)에 무관하게 섹션 구조 자유롭게 변경 가능
-- ============================================================
CREATE TABLE chemistry_reports (
    id              BIGINT      NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    requester_id    BIGINT      NOT NULL                  COMMENT 'FK → users.id. 보고서 발행자',
    partner_id      BIGINT      NOT NULL                  COMMENT 'FK → users.id. 보고서 대상자',
    test_type       VARCHAR(20) NOT NULL DEFAULT 'DISC'   COMMENT '검사 유형 (DISC / MBTI / BIG5 등)',
    report          TEXT        NOT NULL                  COMMENT 'Markdown 형식 케미 보고서 전문. 이름 미포함. 렌더링 시 이름 삽입',
    created_at      DATETIME    NOT NULL                  COMMENT '발행 시각',

    PRIMARY KEY (id),
    KEY idx_chemistry_reports_requester_id (requester_id),
    KEY idx_chemistry_reports_partner_id   (partner_id),
    KEY idx_chemistry_reports_test_type    (test_type),

    CONSTRAINT fk_chemistry_reports_requester
        FOREIGN KEY (requester_id)
        REFERENCES users (id),

    CONSTRAINT fk_chemistry_reports_partner
        FOREIGN KEY (partner_id)
        REFERENCES users (id)
) COMMENT = '케미 보고서. Markdown 단일 TEXT로 검사 유형 무관 확장 가능. 이름 미포함 원문 저장';

-- ============================================================
-- 9. notifications
-- ============================================================
CREATE TABLE notifications (
    id              BIGINT          NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    user_id         BIGINT          NOT NULL                  COMMENT 'FK → users.id. 수신자',
    type            ENUM('CHEMISTRY_REPORT', 'COLLEAGUE_REGISTERED') NOT NULL
                                                              COMMENT 'CHEMISTRY_REPORT: 케미 보고서 발행 / COLLEAGUE_REGISTERED: 동료 등록',
    reference_id    BIGINT          NOT NULL                  COMMENT '관련 엔티티 id (chemistry_reports.id 또는 colleagues.id)',
    message         VARCHAR(255)    NOT NULL                  COMMENT '알림 문구',
    created_at      DATETIME        NOT NULL                  COMMENT '알림 발생 시각',

    PRIMARY KEY (id),
    KEY idx_notifications_user_created (user_id, created_at),

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
) COMMENT = '인앱 알림. 클릭 시 즉시 DELETE. 배치 불필요';

-- statistics 테이블 없음
-- MVP에서는 test_results 직접 집계 쿼리로 대체
-- GET /statistics/comparison → test_results WHERE rater_type='SELF' GROUP BY age_group, gender
-- GET /statistics/trend     → test_results WHERE user_id=? AND rater_type='SELF' AND created_at >= ?
-- 사용자 수만 명 초과 시점에 집계 테이블 또는 Redis 캐싱 도입 검토