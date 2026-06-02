package com.mycpt.backend.domain.result.service;

import com.mycpt.backend.domain.result.entity.DiscCache;
import com.mycpt.backend.domain.result.entity.DiscCacheId;
import com.mycpt.backend.domain.result.repository.DiscCacheRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CacheService 단위 테스트")
class CacheServiceTest {

    @Mock private DiscCacheRepository discCacheRepository;
    @Mock private LlmService llmService;

    // @InjectMocks 미사용 이유:
    //  - CacheService 생성자의 세 번째 인자 ttlDays는 @Value 주입 값이라 @InjectMocks가 처리하지 못함
    //  - 케이스별로 ttlDays를 명시해 생성하면 의도가 더 명확해짐
    private CacheService sut(long ttlDays) {
        return new CacheService(discCacheRepository, llmService, ttlDays);
    }

    // ── 공통 픽스처 ───────────────────────────────────────────────────────────

    private static final long TTL = 365L;
    private static final DiscCacheId ID = new DiscCacheId(2, 1, 3, 2);
    private static final ScoringService.Buckets BUCKETS =
            new ScoringService.Buckets(5, 4, 3, 6);
    private static final String REPORT = "## 결과 개요\n테스트 보고서";
    private static final String NEW_REPORT = "## 결과 개요\n새 보고서";

    // report=NULL로 사전 삽입된 초기 상태 행
    private DiscCache unseeded() {
        return new DiscCache(ID, null, null);
    }

    // ── 행 누락 (데이터 정합성 오류) ─────────────────────────────────────────

    @Nested
    @DisplayName("행 누락 - 초기화 스크립트 미실행")
    class RowMissing {

        @Test
        @DisplayName("[UT-CacheService-보고서생성-행누락예외]")
        void 보고서생성_행누락예외() {
            // given: 초기화 스크립트가 실행되지 않아 행 자체가 없음
            given(discCacheRepository.findById(ID)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> sut(TTL).getReport(BUCKETS))
            // then
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("disc_cache 행 누락");

            verify(llmService, never()).generateReport(any());
        }
    }

    // ── 미생성 (report = NULL) ────────────────────────────────────────────────

    @Nested
    @DisplayName("미생성 - report NULL")
    class Unseeded {

        @Test
        @DisplayName("[UT-CacheService-보고서생성-미생성]")
        void 보고서생성_미생성() {
            // given: 사전 삽입된 행이지만 아직 LLM 보고서가 생성되지 않은 상태
            given(discCacheRepository.findById(ID)).willReturn(Optional.of(unseeded()));
            given(llmService.generateReport(ID)).willReturn(REPORT);
            given(discCacheRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            String result = sut(TTL).getReport(BUCKETS);

            // then
            assertThat(result).isEqualTo(REPORT);
            verify(llmService, times(1)).generateReport(ID);    // LLM 1회 호출
            verify(discCacheRepository, times(1)).save(any());  // UPDATE 1회
        }
    }

    // ── HIT + 유효 ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("캐시 HIT + 유효")
    class HitValid {

        @Test
        @DisplayName("[UT-CacheService-보고서생성-캐시HIT유효]")
        void 보고서생성_캐시HIT유효() {
            // given: 10일 전 생성 -> ttl 365일 이내이므로 유효
            DiscCache fresh = new DiscCache(ID, REPORT, LocalDateTime.now().minusDays(10));
            given(discCacheRepository.findById(ID)).willReturn(Optional.of(fresh));

            // when
            String result = sut(TTL).getReport(BUCKETS);

            // then
            assertThat(result).isEqualTo(REPORT);
            verify(llmService, never()).generateReport(any());  // LLM 미호출
            verify(discCacheRepository, never()).save(any());   // save 없음
        }

        @Test
        @DisplayName("[UT-CacheService-보고서생성-캐시HIT유효경계값분석]")
        void 보고서생성_캐시HIT유효경계값분석() {
            // given: 364일 전 생성
            //  expiredLine = now - 365일
            //  created_at = now - 364일
            //  isBefore(expireLine) -> false -> 유효 처리
            DiscCache boundary = new DiscCache(ID, REPORT, LocalDateTime.now().minusDays(364));
            given(discCacheRepository.findById(ID)).willReturn(Optional.of(boundary));

            // when
            String result = sut(TTL).getReport(BUCKETS);

            // then: 경계는 유효 처리 -> LLM 미호출
            assertThat(result).isEqualTo(REPORT);
            verify(llmService, never()).generateReport(any());
        }
    }

    // ── HIT + 만료 ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("캐시 HIT + 만료")
    class HitExpired {

        @Test
        @DisplayName("[UT-CacheService-보고서생성-캐시HIT만료]")
        void 보고서생성_캐시HIT만료() {
            // given: 366일 전 생성 -> ttl 365일 초과이므로 만료
            DiscCache expired = new DiscCache(ID, REPORT, LocalDateTime.now().minusDays(366));
            given(discCacheRepository.findById(ID)).willReturn(Optional.of(expired));
            given(llmService.generateReport(ID)).willReturn(NEW_REPORT);
            given(discCacheRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            String result = sut(TTL).getReport(BUCKETS);

            // then
            assertThat(result).isEqualTo(NEW_REPORT);   // 새 보고서 반환
            verify(llmService, times(1)).generateReport(ID);    // LLM 1회 호출
            verify(discCacheRepository, times(1)).save(expired);    // 동일 객체를 save (UPDATE)
        }
    }
}