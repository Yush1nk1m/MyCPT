// 타인 평정(assessment) 관련 순수 로직 (assessments/[token]에서 추출)

/** 평정 관련 에러코드 → 서비스 말투 안내 문구. 그 외엔 기본 메시지 */
export function errorMessage(code: string): string {
  if (code === "EXPIRED_CODE") return "초대장이 만료됐어요";
  if (code === "TOKEN_USED") return "이미 응시가 완료된 링크예요";
  if (code === "NOT_FOUND") return "존재하지 않는 링크예요";
  if (code === "INVALID_SCORE")
    return "응답 데이터가 올바르지 않아요. 처음부터 다시 시도해 주세요.";
  return "제출에 실패했어요. 다시 시도해 주세요.";
}
