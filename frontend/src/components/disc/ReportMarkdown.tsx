/**
 * ReportMarkdown
 *
 * DISC 보고서 마크다운 렌더러.
 *
 * 사용처:
 *   - Step3Result (검사 완료 직후 결과 화면)
 *   - /results/[id] (결과 상세 페이지)
 *   - /chemistry/[id] (케미 보고서 상세 페이지)
 */

import ReactMarkdown from "react-markdown";

interface ReportMarkdownProps {
  report: string;
}

export function ReportMarkdown({ report }: ReportMarkdownProps) {
  return (
    <ReactMarkdown
      components={{
        h2: ({ children }) => (
          <h2
            style={{
              display: "flex",
              alignItems: "center",
              gap: 8,
              fontFamily: "var(--font-sans)",
              fontSize: 15,
              fontWeight: 800,
              margin: "22px 0 8px",
              color: "var(--ink)",
            }}
          >
            <span
              style={{
                display: "inline-block",
                width: 8,
                height: 8,
                background: "var(--accent)",
                borderRadius: 2,
                flexShrink: 0,
              }}
            />
            {children}
          </h2>
        ),
        p: ({ children }) => (
          <p
            style={{
              margin: "0 0 10px",
              fontSize: 13,
              lineHeight: 1.7,
              color: "var(--ink-soft)",
            }}
          >
            {children}
          </p>
        ),
        ul: ({ children }) => (
          <ul
            style={{
              margin: "0 0 12px",
              paddingLeft: 18,
              color: "var(--ink-soft)",
              fontSize: 13,
              lineHeight: 1.7,
            }}
          >
            {children}
          </ul>
        ),
        li: ({ children }) => <li style={{ margin: "4px 0" }}>{children}</li>,
        blockquote: ({ children }) => (
          <blockquote
            style={{
              margin: "8px 0 14px",
              borderLeft: "3px solid var(--accent)",
              padding: "4px 12px",
              background: "oklch(0.97 0.03 40)",
              color: "var(--ink)",
              borderRadius: "0 6px 6px 0",
              fontSize: 12.5,
            }}
          >
            {children}
          </blockquote>
        ),
        strong: ({ children }) => (
          <strong style={{ color: "var(--ink)", fontWeight: 700 }}>
            {children}
          </strong>
        ),
        hr: () => (
          <hr
            style={{
              border: "none",
              borderTop: "1px dashed var(--line)",
              margin: "18px 0",
            }}
          />
        ),
      }}
    >
      {report}
    </ReactMarkdown>
  );
}
