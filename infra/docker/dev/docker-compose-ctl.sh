#!/bin/bash

# 에러 발생 시 즉시 스크립트 중단
set -e

# 환경변수 파일 및 컴포즈 기본 명령어 정의
ENV_FILE="../../../.env.dev"
COMPOSE_CMD="docker compose --env-file $ENV_FILE"

# 입력된 인자에 따른 분기 처리
case "$1" in
    up)
        echo "🚀 [Docker] 컨테이너를 빌드 및 생성 후 백그라운드 실행합니다..."
        $COMPOSE_CMD up -d --build
        ;;
    down)
        echo "🛑 [Docker] 컨테이너 및 네트워크를 다운(삭제)합니다..."
        $COMPOSE_CMD down
        ;;
    start)
        echo "▶️ [Docker] 정지된 컨테이너를 다시 시작합니다..."
        $COMPOSE_CMD start
        ;;
    stop)
        echo "⏸️ [Docker] 컨테이너를 일시 정지합니다..."
        $COMPOSE_CMD stop
        ;;
    restart)
        echo "🔄 [Docker] 컨테이너를 재시작합니다..."
        $COMPOSE_CMD stop
        $COMPOSE_CMD start
        ;;
    clean)
        echo "🧹 [Docker] 컨테이너, 네트워크, 볼륨(DB 데이터 포함)을 완전히 격하시킵니다..."
        $COMPOSE_CMD down -v
        ;;
    status)
        echo "📊 [Docker] 현재 컨테이너 실행 상태를 확인합니다..."
        $COMPOSE_CMD ps
        ;;
    *)
        echo "❌ 사용법: $0 {up|down|start|stop|restart|clean|status}"
        exit 1
esac