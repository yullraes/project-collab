import type { ReactNode } from "react";
import "./ActionMenu.css";

export function ActionMenu({
  label,
  children,
}: {
  label: string;
  children: ReactNode;
}) {
  return (
    <details className="more-menu">
      <summary aria-label={label}>•••</summary>
      <div>{children}</div>
    </details>
  );
}
