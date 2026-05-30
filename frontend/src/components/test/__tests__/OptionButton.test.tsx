import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { OptionButton } from "../OptionButton";

describe("OptionButton", () => {
  it("텍스트를 렌더링한다", () => {
    render(
      <OptionButton
        text="목표를 빠르게 달성하고 싶다"
        state="default"
        onClick={vi.fn()}
      />,
    );
    expect(screen.getByText("목표를 빠르게 달성하고 싶다")).toBeInTheDocument();
  });

  it("state=default 일 때 클릭하면 onClick이 호출된다", () => {
    const onClick = vi.fn();
    render(<OptionButton text="옵션" state="default" onClick={onClick} />);
    fireEvent.click(screen.getByRole("button"));
    expect(onClick).toHaveBeenCalledTimes(1);
  });

  it("state=disabled 일 때 클릭해도 onClick이 호출되지 않는다", () => {
    const onClick = vi.fn();
    render(<OptionButton text="옵션" state="disabled" onClick={onClick} />);
    fireEvent.click(screen.getByRole("button"));
    expect(onClick).not.toHaveBeenCalled();
  });

  it("state=selected 일 때 aria-pressed=true 이다", () => {
    render(<OptionButton text="옵션" state="selected" onClick={vi.fn()} />);
    expect(screen.getByRole("button")).toHaveAttribute("aria-pressed", "true");
  });

  it("state=default 일 때 aria-pressed=false 이다", () => {
    render(<OptionButton text="옵션" state="default" onClick={vi.fn()} />);
    expect(screen.getByRole("button")).toHaveAttribute("aria-pressed", "false");
  });
});
