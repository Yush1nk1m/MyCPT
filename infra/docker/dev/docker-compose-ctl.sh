#!/bin/bash

# 에러 발생 시 즉시 스크립트 중단
set -e

# 환경변수 파일 및 컴포즈 기본 명령어 정의
ENV_FILE="../../../.env.dev"
PROFILE="${2:-all}"   # 두 번째 인자 없으면 all 기본값
COMPOSE_CMD="docker compose --env-file $ENV_FILE --profile $PROFILE"

# 입력된 인자에 따른 분기 처리
case "$1" in
    up)
        echo "🚀 [Docker] ($PROFILE) 컨테이너를 빌드 및 생성 후 백그라운드 실행합니다..."
        $COMPOSE_CMD up -d --build
        ;;
    down)
        echo "🛑 [Docker] ($PROFILE) 컨테이너 및 네트워크를 다운(삭제)합니다..."
        $COMPOSE_CMD down
        ;;
    start)
        echo "▶️  [Docker] ($PROFILE) 정지된 컨테이너를 다시 시작합니다..."
        $COMPOSE_CMD start
        ;;
    stop)
        echo "⏸️  [Docker] ($PROFILE) 컨테이너를 일시 정지합니다..."
        $COMPOSE_CMD stop
        ;;
    restart)
        echo "🔄 [Docker] ($PROFILE) 컨테이너를 재시작합니다..."
        $COMPOSE_CMD stop
        $COMPOSE_CMD start
        ;;
    clean)
        echo "🧹 [Docker] ($PROFILE) 컨테이너, 네트워크, 볼륨을 완전히 제거합니다..."
        $COMPOSE_CMD down -v
        ;;
    status)
        echo "📊 [Docker] 현재 컨테이너 실행 상태를 확인합니다..."
        $COMPOSE_CMD ps
        ;;
    *)
        echo "❌ 사용법: $0 {up|down|start|stop|restart|clean|status} [frontend|infra|all]"
        echo "   profile 미지정 시 all (기본값)"
        exit 1
        ;;
esac