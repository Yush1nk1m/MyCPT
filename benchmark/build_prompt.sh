#!/usr/bin/env bash
# §4.2 고정 지시문 템플릿 조립. 4셀 전부 동일 문구, 슬롯만 치환.
# 문서 참조를 유도·억제하는 표현을 넣지 않는다.
# 사용법: build_prompt.sh <target_env_path>  → 프롬프트를 stdout으로 출력
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/config.sh"

target_env="${1:?target_env 경로 필요}"
# shellcheck disable=SC1090
source "$target_env"

spec="$(cat "$FIXTURES_DIR/$SPEC_FIXTURE")"

cat <<EOF
당신은 Spring Boot 백엔드 코드를 구현하는 개발자입니다.

파일 \`${SERVICE_REL}\`의 클래스 \`${CLASS_NAME}\`이(가) 현재 스텁 상태입니다.
메서드 시그니처는 존재하지만 본문이 비어 있어 동작하지 않습니다.

## 과제
이 클래스의 메서드 본문을 구현하여, 아래 테스트가 통과하도록 만드십시오.
- 기준 테스트: \`${TEST_FQCN}\`
- 검증 명령: \`./gradlew test --tests ${TEST_FQCN}\`

## 구현 대상의 원래 명세
${spec}

작업을 완료하면 종료하십시오.
EOF
