import type { ReactNode } from "react";
import "./Notice.css";

export function Notice({
  children,
  title,
  tone = "info",
}: {
  children: ReactNode;
  title?: string;
  tone?: "info" | "warning" | "danger" | "success";
}) {
  return (
    <div className={`notice notice--${tone}`}>
      {title && <strong>{title}</strong>}
      <div>{children}</div>
    </div>
  );
}
