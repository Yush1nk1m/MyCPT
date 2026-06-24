-- ============================================================
-- MyCPT DDL
-- 버전: v0.9
-- 작성일: '26.06.15.
-- 변경: disc_results → disc_tests (JPA @Inheritance(JOINED) 적용)
--        tests.test_type 제거 → dtype 추가 (@DiscriminatorColumn)
--        notifications CTI 적용 (colleague_notifications, chemistry_notifications)
-- ============================================================

-- ============================================================
-- 1. users
-- ============================================================
CREATE TABLE users (
    id                BIGINT            NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    kakao_id          VARCHAR(50)       NOT NULL                  COMMENT '카카오 고유 식별자',
    nickname          VARCHAR(30)       NOT NULL                  COMMENT '서비스 닉네임 (카카오 닉네임 초기값, 수정 가능)',
    profile_image_url VARCHAR(300)      NULL                      COMMENT '프로필 이미지 Full URL. 카카오 기본 이미지 또는 S3 Full URL. NULL이면 기본 이미지 사용',
    birth_year        YEAR              NULL                      COMMENT '로그인 후 프로필 설정 시 입력. NULL이면 미입력 상태',
    gender            ENUM('M','F','N') NULL                      COMMENT 'M: 남성, F: 여성, N: 선택 안 함',
    coins             TINYINT           NOT NULL DEFAULT 3        COMMENT '현재 코인 잔액 (0~3)',
    next_coin_at      DATETIME          NULL                      COMMENT '다음 코인 충전 예정 시각. NULL이면 만충 상태',
    created_at        DATETIME          NOT NULL                  COMMENT '가입 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_kakao_id (kakao_id)
) COMMENT = '회원 정보. 카카오 OAuth 기반 가입';

-- ============================================================
-- 2. disc_cache
--    users/tests 보다 먼저 생성 — disc_tests에서 FK 참조
--    행 DELETE 없이 UPDATE 갱신 → FK 참조 무결성 항상 유지
-- ============================================================
CREATE TABLE disc_cache (
    d           TINYINT  NOT NULL  COMMENT 'D 버킷값 (1~3). 복합 PK 구성',
    i           TINYINT  NOT NULL  COMMENT 'I 버킷값 (1~3)',
    s           TINYINT  NOT NULL  COMMENT 'S 버킷값 (1~3)',
    c           TINYINT  NOT NULL  COMMENT 'C 버킷값 (1~3)',
    report      TEXT     NULL      COMMENT 'Markdown 형식 보고서. NULL=미생성. 이름 미포함. 렌더링 시 이름 삽입',
    created_at  DATETIME NULL      COMMENT '캐시 생성/갱신 시각. NULL=미생성. 온디맨드 만료 판단 기준',

    PRIMARY KEY (d, i, s, c)
) COMMENT = 'DISC 버킷 기반 보고서 캐시. 최대 3^4=81 행. 행 삭제 없이 UPDATE 갱신. Markdown 단일 TEXT';

-- 81개 사전 삽입 (d, i, s, c 각 1~3 전체 조합)
INSERT INTO disc_cache (d, i, s, c, report, created_at) VALUES
(1,1,1,1,NULL,NULL),(1,1,1,2,NULL,NULL),(1,1,1,3,NULL,NULL),
(1,1,2,1,NULL,NULL),(1,1,2,2,NULL,NULL),(1,1,2,3,NULL,NULL),
(1,1,3,1,NULL,NULL),(1,1,3,2,NULL,NULL),(1,1,3,3,NULL,NULL),
(1,2,1,1,NULL,NULL),(1,2,1,2,NULL,NULL),(1,2,1,3,NULL,NULL),
(1,2,2,1,NULL,NULL),(1,2,2,2,NULL,NULL),(1,2,2,3,NULL,NULL),
(1,2,3,1,NULL,NULL),(1,2,3,2,NULL,NULL),(1,2,3,3,NULL,NULL),
(1,3,1,1,NULL,NULL),(1,3,1,2,NULL,NULL),(1,3,1,3,NULL,NULL),
(1,3,2,1,NULL,NULL),(1,3,2,2,NULL,NULL),(1,3,2,3,NULL,NULL),
(1,3,3,1,NULL,NULL),(1,3,3,2,NULL,NULL),(1,3,3,3,NULL,NULL),
(2,1,1,1,NULL,NULL),(2,1,1,2,NULL,NULL),(2,1,1,3,NULL,NULL),
(2,1,2,1,NULL,NULL),(2,1,2,2,NULL,NULL),(2,1,2,3,NULL,NULL),
(2,1,3,1,NULL,NULL),(2,1,3,2,NULL,NULL),(2,1,3,3,NULL,NULL),
(2,2,1,1,NULL,NULL),(2,2,1,2,NULL,NULL),(2,2,1,3,NULL,NULL),
(2,2,2,1,NULL,NULL),(2,2,2,2,NULL,NULL),(2,2,2,3,NULL,NULL),
(2,2,3,1,NULL,NULL),(2,2,3,2,NULL,NULL),(2,2,3,3,NULL,NULL),
(2,3,1,1,NULL,NULL),(2,3,1,2,NULL,NULL),(2,3,1,3,NULL,NULL),
(2,3,2,1,NULL,NULL),(2,3,2,2,NULL,NULL),(2,3,2,3,NULL,NULL),
(2,3,3,1,NULL,NULL),(2,3,3,2,NULL,NULL),(2,3,3,3,NULL,NULL),
(3,1,1,1,NULL,NULL),(3,1,1,2,NULL,NULL),(3,1,1,3,NULL,NULL),
(3,1,2,1,NULL,NULL),(3,1,2,2,NULL,NULL),(3,1,2,3,NULL,NULL),
(3,1,3,1,NULL,NULL),(3,1,3,2,NULL,NULL),(3,1,3,3,NULL,NULL),
(3,2,1,1,NULL,NULL),(3,2,1,2,NULL,NULL),(3,2,1,3,NULL,NULL),
(3,2,2,1,NULL,NULL),(3,2,2,2,NULL,NULL),(3,2,2,3,NULL,NULL),
(3,2,3,1,NULL,NULL),(3,2,3,2,NULL,NULL),(3,2,3,3,NULL,NULL),
(3,3,1,1,NULL,NULL),(3,3,1,2,NULL,NULL),(3,3,1,3,NULL,NULL),
(3,3,2,1,NULL,NULL),(3,3,2,2,NULL,NULL),(3,3,2,3,NULL,NULL),
(3,3,3,1,NULL,NULL),(3,3,3,2,NULL,NULL),(3,3,3,3,NULL,NULL);

-- ============================================================
-- 3. tests
--    JPA @Inheritance(JOINED) 부모 테이블.
--    검사 유형 무관 공통 메타데이터 저장.
--    dtype: JPA @DiscriminatorColumn — 코드가 값의 유효 범위 보장
--    DISC → disc_tests, MBTI → mbti_tests(추후), Big5 → big5_tests(추후)
-- ============================================================
CREATE TABLE tests (
    id          BIGINT               NOT NULL AUTO_INCREMENT            COMMENT '내부 식별자',
    user_id     BIGINT               NOT NULL                           COMMENT 'FK → users.id. 결과 귀속 대상 (피평정자)',
    rater_type  ENUM('SELF','OTHER') NOT NULL DEFAULT 'SELF'            COMMENT 'SELF: 자기 평정 / OTHER: 타인 평정',
    dtype       VARCHAR(20)          NOT NULL                           COMMENT 'JPA @DiscriminatorColumn. DISC / MBTI / BIG5 등. 코드가 유효 범위 보장',
    label       VARCHAR(30)          NULL                               COMMENT '타인 평정 식별 라벨 (예: 여자친구). 자기 평정은 NULL. assessment_tokens.label 에서 복사',
    created_at  DATETIME             NOT NULL                           COMMENT '검사 완료 시각',

    PRIMARY KEY (id),
    KEY idx_tests_user_id    (user_id),
    KEY idx_tests_rater_type (rater_type),

    CONSTRAINT fk_tests_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
) COMMENT = '검사 응시 헤더. 유형 무관 공통 메타데이터. JPA @Inheritance(JOINED) 부모 테이블';

-- ============================================================
-- 4. disc_tests
--    tests JPA JOINED 상속 자식 테이블. DISC 전용 원점수 + 버킷값 저장.
--    test_id: tests.id PK 공유 — JPA @Inheritance(JOINED) 구조
--    MBTI 추가 시 mbti_tests 테이블만 신규 추가. tests 스키마 변경 없음.
-- ============================================================
CREATE TABLE disc_tests (
    test_id  BIGINT  NOT NULL  COMMENT 'PK 겸 FK → tests.id. JOINED 상속 PK 공유',
    d_score  TINYINT NOT NULL  COMMENT 'D 원점수 (-24 ~ +48)',
    i_score  TINYINT NOT NULL  COMMENT 'I 원점수 (-24 ~ +48)',
    s_score  TINYINT NOT NULL  COMMENT 'S 원점수 (-24 ~ +48)',
    c_score  TINYINT NOT NULL  COMMENT 'C 원점수 (-24 ~ +48)',
    d_bucket TINYINT NOT NULL  COMMENT 'D 버킷값 (1~3). disc_cache 복합 FK 구성',
    i_bucket TINYINT NOT NULL  COMMENT 'I 버킷값 (1~3)',
    s_bucket TINYINT NOT NULL  COMMENT 'S 버킷값 (1~3)',
    c_bucket TINYINT NOT NULL  COMMENT 'C 버킷값 (1~3)',

    PRIMARY KEY (test_id),
    KEY idx_disc_tests_cache (d_bucket, i_bucket, s_bucket, c_bucket),

    CONSTRAINT fk_disc_tests_test
        FOREIGN KEY (test_id)
        REFERENCES tests (id),

    CONSTRAINT fk_disc_tests_disc_cache
        FOREIGN KEY (d_bucket, i_bucket, s_bucket, c_bucket)
        REFERENCES disc_cache (d, i, s, c)
) COMMENT = 'DISC 검사 전용 결과. tests JOINED 상속 자식. 원점수 + 버킷값 저장';

-- ============================================================
-- 5. coin_transactions
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
-- 6. peer_codes
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
-- 7. assessment_tokens
--    used=TRUE 처리로 중복 제출 방지
--    만료 토큰은 peer_codes 배치와 통합 삭제
-- ============================================================
CREATE TABLE assessment_tokens (
    id          BIGINT      NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    subject_id  BIGINT      NOT NULL                  COMMENT 'FK → users.id. 평정 대상자 (링크 생성한 회원)',
    token       CHAR(32)    NOT NULL                  COMMENT '일회용 랜덤 토큰',
    label       VARCHAR(30) NULL                      COMMENT '평정자 식별 라벨 (예: 여자친구). 결과 저장 시 tests.label 에 복사',
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
-- 8. colleagues
--    user_a_id < user_b_id 저장 규칙 → UNIQUE (user_a_id, user_b_id) 만으로 중복 방지
-- ============================================================
CREATE TABLE colleagues (
    id          BIGINT      NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    user_a_id   BIGINT      NOT NULL                  COMMENT 'FK → users.id. 두 사용자 중 작은 ID',
    user_b_id   BIGINT      NOT NULL                  COMMENT 'FK → users.id. 두 사용자 중 큰 ID',
    created_at  DATETIME    NOT NULL                  COMMENT '동료 관계 생성 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uq_colleagues_pair (user_a_id, user_b_id),
    KEY idx_colleagues_user_a_id  (user_a_id),
    KEY idx_colleagues_user_b_id  (user_b_id),

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
-- 9. chemistry_reports
-- ============================================================
CREATE TABLE chemistry_reports (
    id              BIGINT      NOT NULL AUTO_INCREMENT            COMMENT '내부 식별자',
    requester_id    BIGINT      NOT NULL                           COMMENT 'FK → users.id. 보고서 발행자',
    partner_id      BIGINT      NOT NULL                           COMMENT 'FK → users.id. 보고서 대상자',
    test_type       VARCHAR(20) NOT NULL DEFAULT 'DISC'            COMMENT '검사 유형 (DISC / MBTI / BIG5 등)',
    status          VARCHAR(20) NOT NULL DEFAULT 'NULL'      COMMENT '발행 상태. NULL(발행 전) / (GENERATING(발행 중) / READY(완료) / ERROR(실패)',
    requester_d     TINYINT     NULL      COMMENT 'requester D 버킷. READY 상태에서만 세팅',
    requester_i     TINYINT     NULL,
    requester_s     TINYINT     NULL,
    requester_c     TINYINT     NULL,
    partner_d       TINYINT     NULL      COMMENT 'partner D 버킷. READY 상태에서만 세팅',
    partner_i       TINYINT     NULL,
    partner_s       TINYINT     NULL,
    partner_c       TINYINT     NULL,
    created_at      DATETIME    NOT NULL                           COMMENT '발행 시각',

    FOREIGN KEY (requester_d, requester_i, requester_s, requester_c,
                 partner_d,   partner_i,   partner_s,   partner_c)
        REFERENCES chemistry_cache (requester_d, requester_i, requester_s, requester_c,
                                    partner_d,   partner_i,   partner_s,   partner_c)

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
-- 10. notifications
--     JPA @Inheritance(JOINED) 부모 테이블.
--     dtype: JPA @DiscriminatorColumn — 알림 유형별 서브타입 식별
--     클릭 시 즉시 DELETE — 배치 불필요
-- ============================================================
CREATE TABLE notifications (
    id          BIGINT       NOT NULL AUTO_INCREMENT   COMMENT '내부 식별자',
    user_id     BIGINT       NOT NULL                  COMMENT 'FK → users.id. 수신자',
    dtype       VARCHAR(30)  NOT NULL                  COMMENT 'JPA @DiscriminatorColumn. COLLEAGUE_REGISTERED / CHEMISTRY_REPORT 등',
    message     VARCHAR(255) NOT NULL                  COMMENT '알림 문구',
    created_at  DATETIME     NOT NULL                  COMMENT '알림 발생 시각',

    PRIMARY KEY (id),
    KEY idx_notifications_user_created (user_id, created_at),

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
) COMMENT = '인앱 알림 공통 헤더. JPA @Inheritance(JOINED) 부모 테이블. 클릭 시 즉시 DELETE';

-- ============================================================
-- 11. colleague_notifications
--     COLLEAGUE_REGISTERED 알림 전용 서브타입.
--     colleague_id → colleagues.id FK로 타입 안전성 확보.
-- ============================================================
CREATE TABLE colleague_notifications (
    id              BIGINT  NOT NULL  COMMENT 'PK 겸 FK → notifications.id. JOINED 상속 PK 공유',
    colleague_id    BIGINT  NOT NULL  COMMENT 'FK → colleagues.id. 등록된 동료 관계',

    PRIMARY KEY (id),

    CONSTRAINT fk_colleague_notifications_notification
        FOREIGN KEY (id)
        REFERENCES notifications (id),

    CONSTRAINT fk_colleague_notifications_colleague
        FOREIGN KEY (colleague_id)
        REFERENCES colleagues (id)
) COMMENT = 'COLLEAGUE_REGISTERED 알림 전용. notifications JOINED 상속 자식';

-- ============================================================
-- 12. chemistry_notifications
--     CHEMISTRY_REPORT 알림 전용 서브타입.
--     chemistry_report_id → chemistry_reports.id FK로 타입 안전성 확보.
-- ============================================================
CREATE TABLE chemistry_notifications (
    id                  BIGINT  NOT NULL  COMMENT 'PK 겸 FK → notifications.id. JOINED 상속 PK 공유',
    chemistry_report_id BIGINT  NOT NULL  COMMENT 'FK → chemistry_reports.id. 완료된 케미 보고서',

    PRIMARY KEY (id),

    CONSTRAINT fk_chemistry_notifications_notification
        FOREIGN KEY (id)
        REFERENCES notifications (id),

    CONSTRAINT fk_chemistry_notifications_chemistry_report
        FOREIGN KEY (chemistry_report_id)
        REFERENCES chemistry_reports (id)
) COMMENT = 'CHEMISTRY_REPORT 알림 전용. notifications JOINED 상속 자식';

-- ============================================================
-- 13. chemistry_cache
--     케미 보고서 Lazy Cache.
--     복합 PK (requester 4축 + partner 4축) — 최대 81×81=6,561행.
--     disc_cache와 달리 사전 삽입 없음 — 미스 시 INSERT, 히트 시 UPDATE.
--     A/B 순서 미정규화 — requester/partner 주어가 다른 보고서이므로 별도 캐시.
-- ============================================================
CREATE TABLE chemistry_cache (
    requester_d  TINYINT   NOT NULL  COMMENT 'requester D 버킷 (1~3)',
    requester_i  TINYINT   NOT NULL  COMMENT 'requester I 버킷 (1~3)',
    requester_s  TINYINT   NOT NULL  COMMENT 'requester S 버킷 (1~3)',
    requester_c  TINYINT   NOT NULL  COMMENT 'requester C 버킷 (1~3)',
    partner_d    TINYINT   NOT NULL  COMMENT 'partner D 버킷 (1~3)',
    partner_i    TINYINT   NOT NULL  COMMENT 'partner I 버킷 (1~3)',
    partner_s    TINYINT   NOT NULL  COMMENT 'partner S 버킷 (1~3)',
    partner_c    TINYINT   NOT NULL  COMMENT 'partner C 버킷 (1~3)',
    report       TEXT      NULL      COMMENT 'Markdown 케미 보고서. NULL=미생성. 이름 미포함',
    created_at   DATETIME  NULL      COMMENT '캐시 생성/갱신 시각. NULL=미생성. 온디맨드 만료 판단 기준',

    PRIMARY KEY (requester_d, requester_i, requester_s, requester_c,
                 partner_d,   partner_i,   partner_s,   partner_c)
) COMMENT = '케미 보고서 Lazy Cache. 복합 PK 8축. 사전 삽입 없음. 최대 6,561행';

-- ============================================================
-- chemistry_cache 초기화 시드 (6,561행 = 81 × 81)
-- requester/partner 각 4축이 1~3 범위의 모든 조합
-- status='NULL', report=NULL, created_at=NULL 으로 사전 삽입
-- ============================================================
INSERT INTO chemistry_cache
    (requester_d, requester_i, requester_s, requester_c,
     partner_d,   partner_i,   partner_s,   partner_c,
     status, report, created_at)
SELECT
    rd, ri, rs, rc,
    pd, pi, ps, pc,
    'NULL', NULL, NULL
FROM
    (SELECT 1 AS rd UNION SELECT 2 UNION SELECT 3) r_d,
    (SELECT 1 AS ri UNION SELECT 2 UNION SELECT 3) r_i,
    (SELECT 1 AS rs UNION SELECT 2 UNION SELECT 3) r_s,
    (SELECT 1 AS rc UNION SELECT 2 UNION SELECT 3) r_c,
    (SELECT 1 AS pd UNION SELECT 2 UNION SELECT 3) p_d,
    (SELECT 1 AS pi UNION SELECT 2 UNION SELECT 3) p_i,
    (SELECT 1 AS ps UNION SELECT 2 UNION SELECT 3) p_s,
    (SELECT 1 AS pc UNION SELECT 2 UNION SELECT 3) p_c;