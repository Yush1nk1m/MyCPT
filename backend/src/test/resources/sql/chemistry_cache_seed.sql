-- chemistry_cache 초기화 시드 (6,561행 = 81 × 81)
-- @DataJpaTest/SpringBootTest는 ddl-auto=create-drop이라 schema.sql의 INSERT가 자동 실행되지 않음
-- ChemistryRepositoryTest, ChemistryTxHelperTest, ChemistryCacheServiceIntegrationTest에서 @Sql로 주입
--
-- requester 4축(rd, ri, rs, rc) × partner 4축(pd, pi, ps, pc) 각 1~3의 전체 조합
-- status='NULL', report=NULL, created_at=NULL 으로 사전 삽입

INSERT INTO chemistry_cache
(requester_d, requester_i, requester_s, requester_c,
 partner_d,   partner_i,   partner_s,   partner_c,
 status, report, created_at, updated_at)
SELECT rd, ri, rs, rc, pd, pi, ps, pc, 'NULL', NULL, NULL, NULL
FROM
    (SELECT 1 AS rd UNION ALL SELECT 2 UNION ALL SELECT 3) t_rd,
    (SELECT 1 AS ri UNION ALL SELECT 2 UNION ALL SELECT 3) t_ri,
    (SELECT 1 AS rs UNION ALL SELECT 2 UNION ALL SELECT 3) t_rs,
    (SELECT 1 AS rc UNION ALL SELECT 2 UNION ALL SELECT 3) t_rc,
    (SELECT 1 AS pd UNION ALL SELECT 2 UNION ALL SELECT 3) t_pd,
    (SELECT 1 AS pi UNION ALL SELECT 2 UNION ALL SELECT 3) t_pi,
    (SELECT 1 AS ps UNION ALL SELECT 2 UNION ALL SELECT 3) t_ps,
    (SELECT 1 AS pc UNION ALL SELECT 2 UNION ALL SELECT 3) t_pc
ON DUPLICATE KEY UPDATE
    status = 'NULL',
    report = NULL,
    created_at = NULL,
    updated_at = NULL;