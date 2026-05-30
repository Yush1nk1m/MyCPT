import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { TestCloseDialog } from "../TestCloseDialog";

describe("TestCloseDialog", () => {
  const defaultProps = {
    answeredCount: 10,
    onConfirm: vi.fn(),
    onDismiss: vi.fn(),
  };

  it("답한 문항 수를 표시한다", () => {
    render(<TestCloseDialog {...defaultProps} />);
    expect(screen.getByText(/10 \/ 24/)).toBeInTheDocument();
  });

  it('"중단하고 닫기" 클릭 시 onConfirm이 호출된다', () => {
    render(<TestCloseDialog {...defaultProps} />);
    fireEvent.click(screen.getByText("중단하고 닫기"));
    expect(defaultProps.onConfirm).toHaveBeenCalledTimes(1);
  });

  it('"계속 응시하기" 클릭 시 onDismiss가 호출된다', () => {
    render(<TestCloseDialog {...defaultProps} />);
    fireEvent.click(screen.getByText("계속 응시하기"));
    expect(defaultProps.onDismiss).toHaveBeenCalledTimes(1);
  });

  it("스크림 클릭 시 onDismiss가 호출된다", () => {
    const { container } = render(<TestCloseDialog {...defaultProps} />);
    // 스크림(최상위 div) 직접 클릭
    fireEvent.click(container.firstChild!);
    expect(defaultProps.onDismiss).toHaveBeenCalled();
  });
});
