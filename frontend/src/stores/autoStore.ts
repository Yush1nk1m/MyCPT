import { create } from "zustand";

interface AuthUser {
  userId: number;
  nickname: string;
  profileImageUrl: string | null;
  coins: number;
  nextCoinAt: string | null;
  birthYear: number | null;
  gender: "M" | "F" | "N" | null;
}

interface AuthState {
  user: AuthUser | null;
  isAuthenticated: boolean;
  setUser: (user: AuthUser | null) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  setUser: (user) => set({ user, isAuthenticated: user !== null }),
  clear: () => set({ user: null, isAuthenticated: false }),
}));
