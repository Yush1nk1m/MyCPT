// 케미 보고서 관련 순수 로직 (chemistry/page에서 추출)

type PeerLabelInput = {
  myRole: "REQUESTER" | "PARTNER";
  partnerNickname: string;
  requesterNickname: string;
};

/** 상대 라벨: "나 ↔ {상대 닉네임}" — myRole에 따라 partner/requester 중 상대방 닉네임을 선택 */
export function peerLabel(r: PeerLabelInput): string {
  const partnerName =
    r.myRole === "REQUESTER" ? r.partnerNickname : r.requesterNickname;
  return `나 ↔ ${partnerName}`;
}
