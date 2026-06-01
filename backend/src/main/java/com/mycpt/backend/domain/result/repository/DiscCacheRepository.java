package com.mycpt.backend.domain.result.repository;

import com.mycpt.backend.domain.result.entity.DiscCache;
import com.mycpt.backend.domain.result.entity.DiscCacheId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * disc_cache 테이블 접근
 *
 * JpaRepository<DiscCache, DiscCacheId> 상속만으로 findById(DiscCacheId), save(DiscCache) 모두 사용 가능
 *
 * CacheService 사용 패턴:
 *  - findById(id) -> HIT/MISS 판단
 *  - save(new DiscCache()) -> MISS 시 INSERT
 *  - save(existing) -> HIT + 만료 시 refresh() 후 UPDATE
 *    (@Transactional 내에서 dirty checking으로 자동 UPDATE되지만, 명시적 save() 호출로 의도 표현)
 */
public interface DiscCacheRepository extends JpaRepository<DiscCache, DiscCacheId> {
}
