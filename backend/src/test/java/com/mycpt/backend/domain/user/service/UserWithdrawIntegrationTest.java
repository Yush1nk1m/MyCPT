package com.mycpt.backend.domain.user.service;

import com.mycpt.backend.common.enums.TestType;
import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.assessment.entity.AssessmentToken;
import com.mycpt.backend.domain.assessment.repository.AssessmentTokenRepository;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import com.mycpt.backend.domain.chemistry.service.ChemistryService;
import com.mycpt.backend.domain.coin.entity.CoinTransaction;
import com.mycpt.backend.domain.coin.enums.CoinReason;
import com.mycpt.backend.domain.coin.repository.CoinTransactionRepository;
import com.mycpt.backend.domain.colleague.entity.Colleague;
import com.mycpt.backend.domain.colleague.entity.PeerCode;
import com.mycpt.backend.domain.colleague.repository.ColleagueRepository;
import com.mycpt.backend.domain.colleague.repository.PeerCodeRepository;
import com.mycpt.backend.domain.colleague.service.ColleagueService;
import com.mycpt.backend.domain.result.entity.DiscTest;
import com.mycpt.backend.domain.result.repository.DiscTestRepository;
import com.mycpt.backend.domain.user.client.KakaoUnlinkClient;
import com.mycpt.backend.domain.user.dto.WithdrawRequest;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import com.mycpt.backend.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@DisplayName("회원 탈퇴 통합 테스트")
@Sql(scripts = "/sql/chemistry_cache_seed.sql")
public class UserWithdrawIntegrationTest extends IntegrationTestSupport {

    @MockitoBean KakaoUnlinkClient kakaoUnlinkClient;

    @Autowired UserService userService;
    @Autowired ChemistryService chemistryService;
    @Autowired ColleagueService colleagueService;
    @Autowired UserRepository userRepository;
    @Autowired ColleagueRepository colleagueRepository;
    @Autowired ChemistryReportRepository chemistryReportRepository;
    @Autowired DiscTestRepository discTestRepository;
    @Autowired CoinTransactionRepository coinTransactionRepository;
    @Autowired PeerCodeRepository peerCodeRepository;
    @Autowired AssessmentTokenRepository assessmentTokenRepository;

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────────────────

    private User stubUser(String suffix) {
        return userRepository.save(User.create("kakao-" + suffix, suffix, null));
    }

    private void createColleague(User a, User b) {
        if (a.getId() > b.getId()) { User t = a; a = b; b = t; }
        colleagueRepository.save(Colleague.create(a, b));
    }

    private void createSelfResult(User user) {
        discTestRepository.save(
                DiscTest.createForSelf(user, 32, 10, -4, -14, 3, 2, 1, 2)
        );
    }

    private ChemistryReport createChemistryReport(User requester, User partner) {
        ChemistryCacheId cacheId = new ChemistryCacheId(3, 2, 1, 2, 3, 2, 1, 2);
        return chemistryReportRepository.save(
                ChemistryReport.create(requester, partner, TestType.DISC, cacheId)
        );
    }

    @Nested
    @DisplayName("withdraw() - 본인 관점")
    class Withdraw {

        @Test
        @DisplayName("[IT-UserWithdraw-전체흐름-성공]")
        void 전체흐름_성공() {
            // given
            User me = stubUser("a");
            User partner = stubUser("b");
            createColleague(me, partner);
            createSelfResult(me);
            coinTransactionRepository.save(CoinTransaction.create(me, 3, CoinReason.SIGNUP, 3));
            peerCodeRepository.save(PeerCode.create(me, 7L));
            AssessmentToken token = AssessmentToken.create(me, "친구", 7);
            assessmentTokenRepository.save(token);
            ChemistryReport report = createChemistryReport(me, partner);

            // when
            userService.withdraw(me.getId(), new WithdrawRequest("테스트 사유"));

            // then
            assertThat(discTestRepository.findAllByUserId(me.getId())).isEmpty();
            assertThat(coinTransactionRepository.findAll())
                    .noneMatch(tx -> tx.getUser().getId().equals(me.getId()));
            assertThat(peerCodeRepository.findByUserId(me.getId())).isEmpty();
            assertThat(assessmentTokenRepository.findByToken(token.getToken())).isEmpty();
            assertThat(colleagueRepository.findAllByUserId(me.getId())).isEmpty();

            // chemistry_reports는 유지
            assertThat(chemistryReportRepository.findById(report.getId())).isPresent();

            // users 행은 존재하되 익명화
            User withdrawn = userRepository.findById(me.getId()).orElseThrow();
            assertThat(withdrawn.getKakaoId()).isNull();
            assertThat(withdrawn.getNickname()).isEqualTo("a");
        }

        @Test
        @DisplayName("[IT-UserWithdraw-카카오unlink실패-전체롤백]")
        void 카카오unlink실패_전체롤백() {
            // given
            User me = stubUser("a");
            createSelfResult(me);
            willThrow(new RuntimeException("카카오 API 오류"))
                    .given(kakaoUnlinkClient).unlink(anyString());

            // when
            assertThatThrownBy(() -> userService.withdraw(me.getId(), null))
                    .isInstanceOf(RuntimeException.class);

            // then - 롤백되어 삭제 안 됨
            assertThat(discTestRepository.findAllByUserId(me.getId())).hasSize(1);
        }
    }

    @Nested
    @DisplayName("withdraw() - 상대방 관점")
    class WithdrawPartnerView {

        @Test
        @DisplayName("[IT-UserWithdraw-상대방동료목록조회-자동제외]")
        void 상대방동료목록조회_자동제외() {
            // given
            User me = stubUser("a");
            User partner = stubUser("b");
            createColleague(me, partner);
            createSelfResult(me);

            // when
            userService.withdraw(me.getId(), null);

            // then
            assertThat(colleagueRepository.findAllByUserId(partner.getId())).isEmpty();
        }

        @Test
        @DisplayName("[IT-UserWithdraw-상대방동료상세조회-FORBIDDEN]")
        void 상대방동료상세조회_FORBIDDEN() {
            // given
            User me = stubUser("a");
            User partner = stubUser("b");
            createColleague(me, partner);
            createSelfResult(me);
            userService.withdraw(me.getId(), null);

            // when
            assertThatThrownBy(() -> colleagueService.get(me.getId(), partner.getId()))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    });
        }

        @Test
        @DisplayName("[IT-UserWithdraw-상대방케미보고서조회-닉네임유지]")
        void 상대방케미보고서조회_닉네임유지() {
            // given
            User me = stubUser("a");
            User partner = stubUser("b");
            createColleague(me, partner);
            createSelfResult(me);
            ChemistryReport report = createChemistryReport(me, partner);

            // when
            userService.withdraw(me.getId(), null);

            // then
            ChemistryReport reloaded = chemistryReportRepository
                    .findByIdWithUsers(report.getId()).orElseThrow();
            assertThat(reloaded.getRequester().getNickname()).isEqualTo("a");
        }

        @Test
        @DisplayName("[IT-UserWithdraw-상대방케미발행시도-FORBIDDEN]")
        void 상대방케미발행시도_FORBIDDEN() {
            // given
            User me = stubUser("a");
            User partner = stubUser("b");
            createColleague(me, partner);
            createSelfResult(me);
            createSelfResult(partner);
            userService.withdraw(me.getId(), null);

            // when
            assertThatThrownBy(() -> chemistryService.issue(partner.getId(), me.getId()))
                    // then
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> {
                        BusinessException be = (BusinessException) e;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    });
        }
    }
}
