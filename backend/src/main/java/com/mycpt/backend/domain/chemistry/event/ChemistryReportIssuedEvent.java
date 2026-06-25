package com.mycpt.backend.domain.chemistry.event;

import com.mycpt.backend.domain.statistics.dto.LatestBuckets;

/**
 * 케미 보고서 발행 요청 완료 이벤트.
 * ChemistryService.issue() 트랜잭션 커밋 직후 ChemistryReportProcessor.process() 트리거용.
 *
 * @Async 직접 호출 시 커밋 이전 실행 가능성이 있어
 * @TransactionalEventListener(AFTER_COMMIT)으로 커밋 후 실행을 보장한다.
 */
public record ChemistryReportIssuedEvent(
        Long reportId,
        LatestBuckets requesterBuckets,
        LatestBuckets partnerBuckets
) {
}
