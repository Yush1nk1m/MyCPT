# Entry-Point — MyCPT 설계 문서 진입점

**문서 버전**: v0.1
**작성일**: '26.07.13.
**작성자**: 김유신

## 변경 이력

| 버전 | 변경 내용 | 날짜       |
| ---- | --------- | ---------- |
| v0.1 | 초안 작성 | '26.07.13. |

---

## 목적

이 문서는 **에이전트의 작업 진입점**이다. 루트 `CLAUDE.md`의 Entry-Point 규약에 따라, 에이전트는 모든 작업 착수 전에 이 문서를 읽고 아래 매핑에서 **작업 속성에 해당하는 설계 문서를 선택해 읽은 후, 그 내용에 근거해서만 작업한다.** 설계 문서에 없는 내용을 추측으로 보충하지 않는다.

## 프로젝트 개요·실행

- **개요·기술 스택·실행 방법**: 루트 `Readme.md` 참조 (MyCPT — DISC 이론 기반 직무 역량 성향 분석 서비스, Spring Boot + Next.js 모노레포)
- **테스트 명령**:
  - 백엔드: `cd backend && ./gradlew test` — Testcontainers 사용, Docker 데몬 필요
  - 프론트엔드: `cd frontend && npx vitest run`

## 작업 속성 → 필독 문서 매핑

| 작업 속성 | 필독 문서 |
| --- | --- |
| 기획·요구사항 파악 | `common/service-design.md`, `common/requirements-design.md` |
| 백엔드 API 추가·수정 | `common/api-design.md`, `common/architecture-design.md`(§4 패키지 구조), `backend/maintenance-guide.md`(버저닝·컨트롤러 네이밍) |
| DB 스키마 변경 | `backend/database-design.md`, `sql/schema.sql` — 두 파일은 항상 동기 유지 |
| 도메인 로직 (채점·버킷·LLM 캐시·케미·코인) | `common/service-design.md` §3(검사 설계)·§4(LLM 비용 최적화), `common/architecture-design.md` §3(데이터 흐름), `UML/` 시퀀스 다이어그램 |
| 테스트 작성 | `common/test-process.md`(프로세스·역할·ID 포맷), `backend/test-design.md`, `frontend/scenario-test-design.md` |
| 프론트엔드 작업 | **`frontend/CLAUDE.md`(→ `frontend/AGENTS.md`) 필독** — 이 Next.js 버전은 학습 데이터와 다를 수 있어 `node_modules/next/dist/docs/` 가이드 참조가 강제됨. 이어서 `frontend/component-map.md`, `design/specs/`(화면·컴포넌트·상태 명세), `design/wireframe/` |
| DISC 척도·문항 | `common/disc-scale-design.md` |
| 일정·진행 현황 파악 | `common/plan.md` |
| 인프라·개발 환경 | `Readme.md` 실행 방법, `infra/docker/dev/` |

여러 속성에 걸치는 작업은 해당 문서를 모두 읽는다. 예: "케미 보고서 API에 필드 추가" → 도메인 로직 + 백엔드 API + (스키마 변경 시) DB 문서.

## 전체 문서 목록

경로는 `docs/` 기준 상대 경로다.

| 문서 | 내용 |
| --- | --- |
| `common/service-design.md` | 서비스 기획 · 검사 설계 · LLM 비용 최적화 전략 (버킷 Lazy Caching, 동시성 방어) |
| `common/requirements-design.md` | 기능/비기능 요구사항 · 유즈케이스 · MVP 범위 |
| `common/architecture-design.md` | 시스템 아키텍처 · 컴포넌트 · 주요 데이터 흐름 · Spring 패키지 구조 |
| `common/api-design.md` | API 명세 |
| `common/disc-scale-design.md` | DISC 척도 · 문항 설계 |
| `common/plan.md` | 개발 계획 · 주차별 실행 기록 |
| `common/test-process.md` | 테스트 설계 프로세스 (테스트 종류 결정 기준, ID 포맷 `[UT\|ST\|IT]-[클래스명축약]-[행위]-[상황]`, 자동화 제외 영역) |
| `backend/database-design.md` | ERD · 테이블 명세 (CTI 패턴) · 인덱스 전략 |
| `backend/test-design.md` | 백엔드 테스트 케이스 ID 체계 · 도메인별 케이스 |
| `backend/maintenance-guide.md` | API 버저닝 · 컨트롤러 네이밍 · 프로필 이미지 저장 · 케미 캐시 실패 복구 구조 |
| `frontend/component-map.md` | 프론트엔드 컴포넌트 맵 |
| `frontend/scenario-test-design.md` | 프론트엔드 시나리오 테스트 설계 |
| `design/specs/` | 화면(screens.yaml) · 컴포넌트(components.yaml) · 상태 머신 · 접근 매트릭스 · 디자인 토큰 |
| `design/wireframe/` | 와이어프레임 (HTML/JSX) |
| `sql/schema.sql` | DDL — `database-design.md`와 동기 유지 |
| `sql/dev_scenario_seed.sql`, `sql/test_dataset.sql` | 개발 시나리오 시드 · 테스트 데이터셋 |
| `UML/` | PlantUML 다이어그램 원본 (아키텍처 · 유즈케이스 · 시퀀스), 렌더링 결과는 `images/` |
| `tmp/` | **PDCA 임시 변경 계획 문서** — Act 완료 후 삭제 (규약은 루트 `CLAUDE.md`) |

## 문서 작성 규칙

- 모든 설계 문서는 헤더에 **문서 버전**(`v0.x`)·작성일·작성자를 표기하고 **변경 이력** 표를 유지한다. 문서를 수정하면 버전을 올리고 변경 이력에 한 줄을 추가한다.
- 문서-코드 정합성은 항상 유지한다. 코드 변경이 설계 문서에 영향을 주면 같은 커밋에서 문서를 함께 갱신한다 (PDCA Do 단계에서 수정안을 tmp 문서에 포함).
- 신규 설계 문서를 추가하면 이 문서(Entry-Point.md)의 목록과 매핑 표에 반드시 등재한다.
