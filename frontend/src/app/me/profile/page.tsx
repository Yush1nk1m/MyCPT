// frontend/src/app/me/profile/page.tsx

"use client";

import { useState, useRef } from "react";
import { useMe } from "@/hooks/useMe";
import { useAuthStore } from "@/stores/autoStore";
import { useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import Image from "next/image";

// screens.yaml validation: nickname min:3 max:20, birthYear 1940~2026
const CURRENT_YEAR = new Date().getFullYear();
const BIRTH_YEARS = Array.from(
  { length: CURRENT_YEAR - 1940 + 1 },
  (_, i) => CURRENT_YEAR - i, // 최신 연도부터 내림차순
);

type Gender = "M" | "F" | "N";
const GENDER_OPTIONS: { value: Gender; label: string }[] = [
  { value: "M", label: "남성" },
  { value: "F", label: "여성" },
  { value: "N", label: "선택 안 함" },
];

function MeProfileForm({
  me,
}: {
  me: NonNullable<ReturnType<typeof useMe>["data"]>;
}) {
  const setUser = useAuthStore((s) => s.setUser);
  const queryClient = useQueryClient();
  const router = useRouter();
  const imageInputRef = useRef<HTMLInputElement>(null);

  // 폼 상태 — me 데이터로 초기화
  const [nickname, setNickname] = useState(me.nickname ?? "");
  const [birthYear, setBirthYear] = useState<number | null>(
    me.birthYear ?? null,
  );
  const [gender, setGender] = useState<Gender | null>(me.gender ?? null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(
    me.profileImageUrl ?? null,
  );
  const [imageFile, setImageFile] = useState<File | null>(null);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 닉네임 유효성: 1~20자
  const isNicknameValid = nickname.length >= 1 && nickname.length <= 20;
  // screens.yaml: nickname/birthYear/gender 모두 required
  const canSave =
    isNicknameValid && birthYear !== null && gender !== null && !saving;

  // 이미지 파일 선택
  function handleImageChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setImageFile(file);
    setPreviewUrl(URL.createObjectURL(file));
  }

  async function handleSave() {
    if (!canSave) return;
    setSaving(true);
    setError(null);

    try {
      // 이미지 먼저 업로드 (선택된 경우에만)
      let profileImageUrl = me?.profileImageUrl ?? null;
      if (imageFile) {
        const formData = new FormData();
        formData.append("image", imageFile);
        const imgRes = await fetch("/api/v1/users/me/profile-image", {
          method: "POST",
          credentials: "include",
          body: formData,
        });
        if (!imgRes.ok) throw new Error("이미지 업로드에 실패했어요.");
        const imgData = await imgRes.json();
        profileImageUrl = imgData.profileImageUrl;
      }

      // 프로필 정보 업데이트
      const patchRes = await fetch("/api/v1/users/me", {
        method: "PATCH",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          nickname,
          birthYear,
          gender,
        }),
      });
      if (!patchRes.ok) throw new Error("저장에 실패했어요.");

      // authStore + React Query 캐시 갱신
      const patchData = await patchRes.json();
      if (me) {
        const updated = {
          ...me,
          nickname: patchData.nickname,
          birthYear: patchData.birthYear,
          gender: patchData.gender,
          profileImageUrl,
        };
        setUser(updated);
        queryClient.setQueryData(["me"], updated);
      }

      router.push("/me");
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "알 수 없는 오류가 발생했어요.",
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="flex flex-col min-h-screen bg-paper">
      {/* 뒤로가기 바 */}
      <div className="flex items-center gap-3 px-4 py-3 bg-white border-b border-line">
        <button onClick={() => router.back()} className="text-ink p-1">
          ‹
        </button>
        <div>
          <p className="font-bold text-sm text-ink">내 정보 수정</p>
          <p className="text-xs text-ink-soft">필수 정보만</p>
        </div>
      </div>

      <div className="flex flex-col gap-4 px-4 py-4">
        {/* 프로필 이미지 */}
        <div className="flex items-center gap-4 bg-white border border-line rounded-xl p-4">
          <div className="w-16 h-16 rounded-full overflow-hidden bg-paper-2 border border-line shrink-0">
            {previewUrl ? (
              <Image
                src={previewUrl}
                alt="프로필 이미지"
                width={64}
                height={64}
                className="object-cover"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-ink-faint text-xs text-center leading-tight">
                프로필
                <br />
                이미지
              </div>
            )}
          </div>
          <div className="flex-1">
            <p className="text-sm font-bold mb-1">프로필 이미지</p>
            <p className="text-xs text-ink-faint leading-relaxed">
              jpg, png, webp · 최대 10MB
            </p>
          </div>
          <button
            onClick={() => imageInputRef.current?.click()}
            className="px-3.5 py-2 text-xs font-semibold border border-line rounded-lg text-ink shrink-0"
          >
            변경
          </button>
          <input
            ref={imageInputRef}
            type="file"
            accept="image/jpeg,image/png,image/webp"
            className="hidden"
            onChange={handleImageChange}
          />
        </div>

        {/* 닉네임 */}
        <div className="flex flex-col gap-1">
          <label className="text-xs font-semibold text-ink">
            닉네임 <span className="text-accent">*</span>
          </label>
          <div className="flex items-center bg-white border border-line rounded-xl px-4 py-3 gap-2">
            <input
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              maxLength={20}
              placeholder="닉네임 입력"
              className="flex-1 text-sm text-ink bg-transparent outline-none placeholder:text-ink-faint"
            />
            <span className="text-xs text-ink-faint font-mono shrink-0">
              {nickname.length} / 20
            </span>
          </div>
          <p className="text-xs text-ink-faint px-1">
            동료와 케미 보고서에 표시되는 이름이에요.
          </p>
        </div>

        {/* 출생 연도 */}
        <div className="flex flex-col gap-1">
          <label className="text-xs font-semibold text-ink">
            출생 연도 <span className="text-accent">*</span>
          </label>
          <div className="bg-white border border-line rounded-xl px-4 py-3">
            <select
              value={birthYear ?? ""}
              onChange={(e) =>
                setBirthYear(
                  e.target.value === "" ? null : Number(e.target.value),
                )
              }
              className="w-full text-sm text-ink bg-transparent outline-none"
            >
              <option value="">연도 선택</option>
              {BIRTH_YEARS.map((y) => (
                <option key={y} value={y}>
                  {y}
                </option>
              ))}
            </select>
          </div>
          <p className="text-xs text-ink-faint px-1">
            통계 비교(20대/30대 평균)에 사용돼요.
          </p>
        </div>

        {/* 성별 */}
        <div className="flex flex-col gap-1">
          <label className="text-xs font-semibold text-ink">
            성별 <span className="text-accent">*</span>
          </label>
          <div className="grid grid-cols-3 gap-1.5">
            {GENDER_OPTIONS.map((opt) => (
              <button
                key={opt.value}
                onClick={() => setGender(opt.value)}
                className={[
                  "py-2.5 text-sm rounded-xl border font-medium transition-colors",
                  gender === opt.value
                    ? "bg-accent text-white border-accent"
                    : "bg-white text-ink border-line",
                ].join(" ")}
              >
                {opt.label}
              </button>
            ))}
          </div>
          <p className="text-xs text-ink-faint px-1">
            통계에만 사용되고 다른 사용자에게 보이지 않아요.
          </p>
        </div>

        {/* 안내 박스 */}
        {(birthYear === null || gender === null) && (
          <div className="bg-amber-50 border border-dashed border-accent rounded-xl px-3 py-2.5 text-xs text-ink-soft leading-relaxed">
            <b className="text-ink">안내</b> · 생년·성별을 비워두면{" "}
            <b className="text-ink">통계 비교</b> 기능이 제한돼요.
          </div>
        )}

        {/* 에러 메시지 */}
        {error && <p className="text-xs text-red-400 text-center">{error}</p>}

        {/* 저장 버튼 */}
        <button
          onClick={handleSave}
          disabled={!canSave}
          className={[
            "w-full py-3.5 rounded-2xl font-bold text-sm transition-opacity",
            canSave ? "bg-ink text-white" : "bg-ink text-white opacity-30",
          ].join(" ")}
        >
          {saving ? "저장 중…" : "저장"}
        </button>

        <div className="h-6" />
      </div>
    </div>
  );
}

export default function MeProfilePage() {
  const { data: me, isLoading } = useMe();

  if (isLoading || !me) {
    return (
      <div className="flex flex-col min-h-screen bg-paper">
        <div className="flex items-center gap-3 px-4 py-3 bg-white border-b border-line">
          <div className="h-4 w-24 rounded bg-paper-2 animate-pulse" />
        </div>
        <div className="flex flex-col gap-4 px-4 py-4">
          <div className="h-20 rounded-xl bg-paper-2 animate-pulse" />
          <div className="h-14 rounded-xl bg-paper-2 animate-pulse" />
          <div className="h-14 rounded-xl bg-paper-2 animate-pulse" />
        </div>
      </div>
    );
  }

  return <MeProfileForm me={me} />;
}
