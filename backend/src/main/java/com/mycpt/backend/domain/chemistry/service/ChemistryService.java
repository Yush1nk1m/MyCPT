package com.mycpt.backend.domain.chemistry.service;

import com.mycpt.backend.common.enums.TestType;
import com.mycpt.backend.common.exception.BusinessException;
import com.mycpt.backend.common.exception.ErrorCode;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportDetail;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportListResponse;
import com.mycpt.backend.domain.chemistry.dto.ChemistryReportSummary;
import com.mycpt.backend.domain.chemistry.entity.ChemistryReport;
import com.mycpt.backend.domain.chemistry.enums.ChemistryReportStatus;
import com.mycpt.backend.domain.chemistry.repository.ChemistryReportRepository;
import com.mycpt.backend.domain.coin.enums.CoinReason;
import com.mycpt.backend.domain.coin.service.CoinService;
import com.mycpt.backend.domain.colleague.repository.ColleagueRepository;
import com.mycpt.backend.domain.user.entity.User;
import com.mycpt.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public void issue(Long requesterId, Long partnerId) {
        Long idA = Math.min(requesterId, partnerId);
        Long idB = Math.max(requesterId, partnerId);
        if (!colleagueRepository.existsByPair(idA, idB)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        coinService.deduct(requesterId, CoinReason.CHEMISTRY_REPORT);

        User requester = userRepository.getReferenceById(requesterId);
        User partner = userRepository.getReferenceById(partnerId);
        ChemistryReport report = ChemistryReport.create(requester, partner, TestType.DISC);
        chemistryReportRepository.save(report);

        // @Async 트리거 - 트랜잭션 커밋 후 별도 스레드에서 실행
        chemistryReportProcessor.process(report.getId());
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
