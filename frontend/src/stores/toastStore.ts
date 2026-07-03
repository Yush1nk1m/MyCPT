import { create } from "zustand";

interface ToastAction {
  label: string;
  onClick: () => void;
}

interface ShowOptions {
  duration?: number;
  action?: ToastAction;
}

interface ToastState {
  message: string | null;
  action: ToastAction | null;
  show: (message: string, options?: ShowOptions) => void;
  hide: () => void;
}

let timer: ReturnType<typeof setTimeout> | undefined;

export const useToastStore = create<ToastState>((set) => ({
  message: null,
  action: null,
  show: (message, options = {}) => {
    clearTimeout(timer);
    // 액션 있으면 5초, 없으면 기존 기본값 2초 유지 (와이어프레임 스펙과 일치)
    const duration = options.duration ?? (options.action ? 5000 : 2000);
    set({ message, action: options.action ?? null });
    timer = setTimeout(() => set({ message: null, action: null }), duration);
  },
  hide: () => {
    clearTimeout(timer);
    set({ message: null, action: null });
  },
}));
