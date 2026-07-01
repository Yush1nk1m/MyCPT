"use client";

import { use, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { DotProgressBar } from "@/components/test/DotProgressBar";
import { QuestionCard } from "@/components/test/QuestionCard";
import {
  DISC_QUESTIONS,
  TOTAL_QUESTIONS,
  toRaterStem,
} from "@/lib/disc/questions";
import type { DiscTag } from "@/lib/disc/questions";
import { calculateScores, validateScores } from "@/lib/disc/scoring";
import type { Answers, DiscScores } from "@/lib/disc/scoring";

// ── 타입 ──────────────────────────────────────────────────────────────────────

interface SubjectInfo {
  subjectNickname: string;
  subjectProfileImageUrl: string | null;
}

type PageState = "intro" | "answering" | "submitting" | "done" | "submit_error";

// ── fetch ─────────────────────────────────────────────────────────────────────

async function fetchSubjectInfo(token: string): Promise<SubjectInfo> {
  const res = await fetch(`/api/v1/assessments/${token}`);
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.code ?? "FETCH_ERROR");
  }
  return res.json();
}

async function submitAssessment(
  token: string,
  scores: DiscScores,
): Promise<void> {
  const res = await fetch(`/api/v1/assessments/${token}/submit`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ scores }),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.code ?? "FETCH_ERROR");
  }
}

// ── 유틸 ─────────────────────────────────────────────────────────────────────

function errorMessage(code: string): string {
  if (code === "EXPIRED_CODE") return "초대장이 만료됐어요";
  if (code === "TOKEN_USED") return "이미 응시가 완료된 링크예요";
  if (code === "NOT_FOUND") return "존재하지 않는 링크예요";
  if (code === "INVALID_SCORE")
    return "응답 데이터가 올바르지 않아요. 처음부터 다시 시도해 주세요.";
  return "제출에 실패했어요. 다시 시도해 주세요.";
}

function Avatar({
  nickname,
  profileImageUrl,
}: {
  nickname: string;
  profileImageUrl: string | null;
}) {
  if (profileImageUrl) {
    return (
      <img
        src={profileImageUrl}
        alt={nickname}
        className="w-20 h-20 rounded-full object-cover border border-[var(--line)]"
      />
    );
  }
  return (
    <div className="w-20 h-20 rounded-full bg-[var(--paper-2)] border border-[var(--line)] flex items-center justify-center text-[24px] font-bold text-[var(--ink-soft)]">
      {nickname.charAt(0)}
    </div>
  );
}

// ── 페이지 ────────────────────────────────────────────────────────────────────

export default function AssessmentPage({
  params,
}: {
  params: Promise<{ token: string }>;
}) {
  const { token } = use(params);
  const [pageState, setPageState] = useState<PageState>("intro");
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState<Answers>({});
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    data: info,
    status: subjectStatus,
    error: subjectError,
  } = useQuery({
    queryKey: ["assessment-subject", token],
    queryFn: () => fetchSubjectInfo(token),
    retry: false,
  });

  const answeredCount = Object.keys(answers).length;
  const currentAnswer = answers[currentIndex];
  const isCurrentAnswered = Boolean(
    currentAnswer?.most && currentAnswer?.least,
  );
  const isLastQuestion = currentIndex === TOTAL_QUESTIONS - 1;

  function handleAnswer(most: DiscTag, least: DiscTag) {
    setAnswers((prev) => ({ ...prev, [currentIndex]: { most, least } }));
  }

  function handlePrev() {
    if (currentIndex > 0) setCurrentIndex((i) => i - 1);
  }

  // 제출 실행 (최초 제출 / 재시도 공통)
  async function doSubmit(scores: DiscScores) {
    setPageState("submitting");
    try {
      await submitAssessment(token, scores);
      setPageState("done");
    } catch (e) {
      setSubmitError(e instanceof Error ? e.message : "FETCH_ERROR");
      setPageState("submit_error");
    }
  }

  async function handleNext() {
    if (!isLastQuestion) {
      setCurrentIndex((i) => i + 1);
      return;
    }
    // 마지막 문항 -> 채점 검증 후 제출
    const scores = calculateScores(answers);
    if (!validateScores(scores)) {
      setSubmitError("INVALID_SCORE");
      setPageState("submit_error");
      return;
    }
    await doSubmit(scores);
  }

  function handleRetry() {
    setSubmitError(null);
    doSubmit(calculateScores(answers));
  }

  // 로딩
  if (subjectStatus === "pending") {
    return (
      <div className="flex flex-col items-center gap-4 px-6 pt-24">
        <div className="w-20 h-20 rounded-full bg-[var(--paper-2)] animate-pulse" />
        <div className="h-4 w-40 rounded bg-[var(--paper-2)] animate-pulse" />
      </div>
    );
  }

  // 토큰 무효 (만료 / 사용됨 / 없음)
  if (subjectStatus === "error") {
    return (
      <div className="flex flex-col items-center gap-2 px-6 pt-24 text-center">
        <p className="text-[15px] font-bold text-[var(--ink)]">
          {errorMessage(subjectError.message)}
        </p>
      </div>
    );
  }

  // 응시 완료
  if (pageState === "done") {
    return (
      <div className="flex flex-col items-center gap-3 px-6 pt-24 text-center">
        <p className="text-[18px] font-bold text-[var(--ink)]">감사합니다 🙏</p>
        <p className="text-[13px] text-[var(--ink-soft)]">
          {info.subjectNickname}님에게 소중한 평정이 전달됐어요.
        </p>
      </div>
    );
  }

  // 제출 실패
  if (pageState === "submit_error") {
    return (
      <div className="flex flex-col items-center gap-3 px-6 pt-24 text-center">
        <p className="text-[15px] font-bold text-[var(--ink)]">
          {errorMessage(submitError ?? "")}
        </p>
        <button
          onClick={handleRetry}
          className="mt-2 px-6 py-2.5 rounded-pill bg-[var(--ink)] text-white text-[13px] font-bold"
        >
          다시 시도하기
        </button>
      </div>
    );
  }

  // 제출 중
  if (pageState === "submitting") {
    return (
      <div className="flex flex-col items-center gap-3 px-6 pt-24 text-center">
        <div className="w-8 h-8 rounded-full border-2 border-[var(--line)] border-t-[var(--ink)] animate-spin" />
        <p className="text-[13px] text-[var(--ink-soft)]">제출하는 중…</p>
      </div>
    );
  }

  // 응시 중
  if (pageState === "answering") {
    const question = DISC_QUESTIONS[currentIndex];
    const raterStem = toRaterStem(
      question.stem,
      currentIndex,
      info.subjectNickname,
    );

    return (
      <div className="flex flex-col min-h-screen px-5 pt-6 pb-6 gap-4 max-w-[480px] mx-auto">
        <DotProgressBar
          total={TOTAL_QUESTIONS}
          currentIndex={currentIndex}
          answeredCount={answeredCount}
        />
        <p className="font-mono text-xs text-[var(--ink-faint)] text-center">
          {currentIndex + 1}번 문항
        </p>
        <QuestionCard
          key={currentIndex}
          questionIndex={currentIndex}
          stem={raterStem}
          options={question.options}
          initialMost={currentAnswer?.most}
          initialLeast={currentAnswer?.least}
          onAnswer={handleAnswer}
        />
        <div className="grid grid-cols-[1fr_2fr] gap-2 mt-auto pt-2">
          <button
            onClick={handlePrev}
            disabled={currentIndex === 0}
            className="py-3 rounded-pill border border-[var(--line)] text-[var(--ink)] font-semibold text-base disabled:opacity-30"
          >
            ‹ 이전
          </button>
          <button
            onClick={handleNext}
            disabled={!isCurrentAnswered}
            className="py-3 rounded-pill bg-[var(--ink)] text-white font-semibold text-base disabled:opacity-30"
          >
            {isLastQuestion ? "제출하기" : "다음 ›"}
          </button>
        </div>
      </div>
    );
  }

  // 인트로 (기본 상태)
  return (
    <div className="flex flex-col items-center gap-4 px-6 pt-20 pb-10 max-w-[400px] mx-auto text-center">
      <Avatar
        nickname={info.subjectNickname}
        profileImageUrl={info.subjectProfileImageUrl}
      />
      <p className="text-[16px] font-bold text-[var(--ink)]">
        {info.subjectNickname}님이 당신에게 평정을 요청했어요
      </p>
      <p className="text-[13px] text-[var(--ink-soft)] leading-relaxed">
        24개의 문항에 답하면서 {info.subjectNickname}님의 성향을 평정해 주세요.
        <br />약 3분 정도 걸려요.
      </p>
      <div className="w-full flex flex-col gap-2.5 mt-2 text-left">
        <div className="flex items-start gap-2 text-[13px] text-[var(--ink-soft)]">
          <span className="text-[var(--accent)]">✓</span>
          <span>회원가입 없이 바로 응시할 수 있어요</span>
        </div>
        <div className="flex items-start gap-2 text-[13px] text-[var(--ink-soft)]">
          <span className="text-[var(--accent)]">✓</span>
          <span>답변은 {info.subjectNickname}님에게만 전달돼요</span>
        </div>
      </div>
      <button
        onClick={() => setPageState("answering")}
        className="w-full mt-4 py-3 rounded-pill bg-[var(--ink)] text-white text-[14px] font-bold"
      >
        시작하기
      </button>
    </div>
  );
}
