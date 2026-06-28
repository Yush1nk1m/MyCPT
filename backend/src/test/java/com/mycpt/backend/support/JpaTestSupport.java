package com.mycpt.backend.support;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @DataJpaTest 슬라이스 테스트 베이스 클래스
 *
 * 역할:
 *  - Testcontainers MySQL 컨테이너 라이프사이클 관리 (클래스 간 컨테이너 재사용)
 *  - @AutoConfigureTestDatabase(replace = NONE): 임데비브 DB 대신 Testcontainers MySQL 사용
 *  - @DynamicPropertySource: 랜덤 포트로 뜬 컨테이너의 datasource를 Spring에 주입
 *
 * MvcTestSupport와의 차이점:
 *  - MvcTestSupport: @WebMvcTest + Security 설정 + MockMvc 헬퍼
 *  - JpaTestSupport: @DataJpaTest + Testcontainers MySQL + datasource 오버라이드
 *
 * 사용법:
 * @Sql(scripts = "/sql/...")로 필요한 시드 데이터를 각 테스트 클래스에서 선언한다.
 * 공통 시드가 없으므로 베이스 클래스에 @Sql을 두지 않는다.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class JpaTestSupport {

    // static: 상속받는 모든 테스트 클래스가 컨테이너 1개를 공유
    // IntegrationTestSupport 패턴과 동일
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("mycpt_test")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }
}
