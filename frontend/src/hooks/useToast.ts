import { useToastStore } from "../stores/toastStore";

export function useToast() {
  return useToastStore((s) => s.show);
}
