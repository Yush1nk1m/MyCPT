import { create } from "zustand";

interface ToastState {
  message: string | null;
  show: (message: string, duration?: number) => void;
  hide: () => void;
}

export const useToastStore = create<ToastState>((set) => ({
  message: null,
  show: (message, duration = 2000) => {
    set({ message });
    setTimeout(() => set({ message: null }), duration);
  },
  hide: () => set({ message: null }),
}));
