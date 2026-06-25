package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.common.enums.TestType;
import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportDetail;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportListResponse;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportSummary;
import com.mycpt.backend.domain.chemistry.entity.ChemistryCacheId;
import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.chemistry.enums.ChemistryReportStatus;
import com.mycpt.backend.domain.chemistry.event.ChemistryReportIssuedEvent;
import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import com.mycpt.backend.domain.coin.enums.CoinReason;
import com.mycpt.backend.domain.coin.service.CoinService;
import com.mycpt.backend.domain.colleague.repository.ColleagueRepository;
import com.mycpt.backend.domain.result.enums.RaterType;
import com.mycpt.backend.domain.statistics.dto.LatestBuckets;
import com.mycpt.backend.domain.statistics.repository.StatisticsRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChemistryService {

    private final ChemistryReportRepository chemistryReportRepository;
    private final ColleagueRepository colleagueRepository;
    private final UserRepository userRepository;
    private final CoinService coinService;
    private final ChemistryReportProcessor chemistryReportProcessor;
    private final StatisticsRepository statisticsRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 케미 보고서 발행 요청.
     *
     * chemistry_reports INSERT는 chemistry_cache 락 획득 이후
     * ChemistryReportProcessor 내부에서 수행.
     * 여기서는 동료 검증 + 코인 차감 + @Async 트리거만 담당.
     *
     * @Async는 트랜잭션 커밋 후 실행되므로 코인 차감이 롤백되는 경우
     * processor가 트리거되지 않음이 보장됨.
     */
    @Transactional
    public void issue(Long requesterId, Long partnerId) {
        Long idA = Math.min(requesterId, partnerId);
        Long idB = Math.max(requesterId, partnerId);
        if (!colleagueRepository.existsByPair(idA, idB)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 버킷 조회 - 없으면 202 이전에 즉시 4xx 반환
        LatestBuckets requesterBuckets = statisticsRepository
                .findLatestBuckets(requesterId, RaterType.SELF, PageRequest.of(0, 1))
                .stream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_RESULT));
        LatestBuckets partnerBuckets = statisticsRepository
                .findLatestBuckets(partnerId, RaterType.SELF, PageRequest.of(0, 1))
                .stream().findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_RESULT));

        ChemistryCacheId cacheId = new ChemistryCacheId(
                requesterBuckets.dBucket(), requesterBuckets.iBucket(),
                requesterBuckets.sBucket(), requesterBuckets.sBucket(),
                partnerBuckets.dBucket(), partnerBuckets.iBucket(),
                partnerBuckets.sBucket(), partnerBuckets.cBucket()
        );

        coinService.deduct(requesterId, CoinReason.CHEMISTRY_REPORT);

        // chemistry_reports INSERT - FK 컬럼(버킷 값)은 READY 시 세팅되므로
        // 이 시점에서는 NULL. seeding으로 chemistry_cache 전 행이 존재하므로 FK 무결성 보장
        User requester = userRepository.getReferenceById(requesterId);
        User partner = userRepository.getReferenceById(partnerId);
        ChemistryReport report = ChemistryReport.create(requester, partner, TestType.DISC, cacheId);
        chemistryReportRepository.save(report);

        // @Async 트리거 - 트랜잭션 커밋 후 별도 스레드에서 실행
        applicationEventPublisher.publishEvent(
                new ChemistryReportIssuedEvent(report.getId(), requesterBuckets, partnerBuckets)
        );
    }

    @Transactional(readOnly = true)
    public ChemistryReportListResponse list(Long userId, Long partnerId, Long cursor, int size) {
        List<ChemistryReport> rows = chemistryReportRepository.findByUserIdWithCursor(
                userId,
                partnerId,
                ChemistryReportStatus.ERROR,
                cursor,
                PageRequest.of(0, size + 1)
        );

        boolean hasNext = rows.size() > size;
        List<ChemistryReport> page = hasNext ? rows.subList(0, size) : rows;

        return new ChemistryReportListResponse(
                page.stream().map(ChemistryReportSummary::from).toList(),
                hasNext ? page.getLast().getId() : null,
                hasNext
        );
    }

    @Transactional(readOnly = true)
    public ChemistryReportDetail get(Long reportId, Long userId) {
        ChemistryReport report = chemistryReportRepository.findByIdWithUsers(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        boolean isParticipant = report.getRequester().getId().equals(userId)
                || report.getPartner().getId().equals(userId);
        if (!isParticipant) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return ChemistryReportDetail.from(report);
    }
}
