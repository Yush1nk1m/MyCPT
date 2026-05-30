/**
 * Step2Answering
 *
 * 검사 시트 Step 2 — 응시 중 화면 (screens.yaml: test-sheet-step2).
 *
 * 레이아웃:
 *   DotProgressBar (24개 dot)
 *   문항 번호 텍스트
 *   QuestionCard (Most/Least 선택)
 *   이전/다음(제출) 버튼
 *
 * QuestionCard에 key={currentIndex}를 명시해
 * 문항 이동 시 컴포넌트를 리마운트시킴 (useEffect 동기화 안티패턴 방지).
 */

import { DotProgressBar } from "../DotProgressBar";
import { QuestionCard } from "../QuestionCard";
import { DISC_QUESTIONS, TOTAL_QUESTIONS } from "@/lib/disc/questions";
import type { DiscTag } from "@/lib/disc/questions";

interface Step2AnsweringProps {
  currentIndex: number;
  answeredCount: number;
  currentAnswer: { most: DiscTag; least: DiscTag } | undefined;
  onAnswer: (most: DiscTag, least: DiscTag) => void;
  onNext: () => void;
  onPrev: () => void;
}

export function Step2Answering({
  currentIndex,
  answeredCount,
  currentAnswer,
  onAnswer,
  onNext,
  onPrev,
}: Step2AnsweringProps) {
  const question = DISC_QUESTIONS[currentIndex];
  const isCurrentAnswered = Boolean(
    currentAnswer?.most && currentAnswer?.least,
  );
  const isLastQuestion = currentIndex === TOTAL_QUESTIONS - 1;
  const isFirstQuestion = currentIndex === 0;

  return (
    <>
      {/* 진행 도트 */}
      <DotProgressBar
        total={TOTAL_QUESTIONS}
        currentIndex={currentIndex}
        answeredCount={answeredCount}
      />

      {/* 문항 번호 */}
      <p className="font-mono text-xs text-ink-faint text-center">
        {currentIndex + 1}번 문항
      </p>

      {/* 문항 카드 — key로 문항 이동 시 리마운트 */}
      <QuestionCard
        key={currentIndex}
        questionIndex={currentIndex}
        stem={question.stem}
        options={question.options}
        initialMost={currentAnswer?.most}
        initialLeast={currentAnswer?.least}
        onAnswer={onAnswer}
      />

      {/* 이전 / 다음(제출) 버튼 */}
      <div className="grid grid-cols-[1fr_2fr] gap-2 mt-auto pt-2">
        <button
          onClick={onPrev}
          disabled={isFirstQuestion}
          className="py-3 rounded-pill border border-line text-ink font-semibold text-base disabled:opacity-30 transition-opacity active:bg-paper-2"
        >
          ‹ 이전
        </button>
        <button
          onClick={onNext}
          disabled={!isCurrentAnswered}
          className="py-3 rounded-pill bg-ink text-white font-semibold text-base disabled:opacity-30 transition-opacity active:opacity-70"
        >
          {isLastQuestion ? "제출하기" : "다음 ›"}
        </button>
      </div>
    </>
  );
}
