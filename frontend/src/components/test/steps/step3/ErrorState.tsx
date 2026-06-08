interface ErrorStateProps {
  message: string | null;
  onRetry: () => void;
}

export function ErrorState({ message, onRetry }: ErrorStateProps) {
  return (
    <div className="flex-1 flex flex-col items-center justify-center gap-4 text-center px-4">
      <span className="text-4xl">😥</span>
      <p className="text-base font-semibold text-ink">
        결과를 불러오지 못했어요
      </p>
      <p className="text-sm text-ink-soft">{message}</p>
      <button
        onClick={onRetry}
        className="px-6 py-3 rounded-pill bg-ink text-white font-semibold text-sm"
      >
        다시 시도하기
      </button>
    </div>
  );
}
